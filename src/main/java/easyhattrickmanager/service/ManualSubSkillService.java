package easyhattrickmanager.service;

import easyhattrickmanager.controller.model.ManualSubSkillRequest;
import easyhattrickmanager.controller.model.ManualSubSkillRequest.Change;
import easyhattrickmanager.repository.UserDAO;
import easyhattrickmanager.repository.TeamDAO;
import easyhattrickmanager.repository.LeagueDAO;
import easyhattrickmanager.repository.PlayerDAO;
import easyhattrickmanager.repository.PlayerDataDAO;
import easyhattrickmanager.repository.PlayerSubSkillDAO;
import easyhattrickmanager.repository.PlayerFormDAO;
import easyhattrickmanager.repository.PlayerExpDAO;
import easyhattrickmanager.repository.ManualSubSkillDAO;
import easyhattrickmanager.repository.model.User;
import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.repository.model.League;
import easyhattrickmanager.repository.model.Player;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.repository.model.PlayerSubSkill;
import easyhattrickmanager.repository.model.PlayerForm;
import easyhattrickmanager.repository.model.PlayerExp;
import easyhattrickmanager.service.model.HTMS;
import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.dataresponse.mapper.PlayerInfoMapper;
import easyhattrickmanager.utils.HTMSUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ToDoubleFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ManualSubSkillService {
    private final UserDAO userDAO;
    private final TeamDAO teamDAO;
    private final LeagueDAO leagueDAO;
    private final PlayerDAO playerDAO;
    private final PlayerDataDAO playerDataDAO;
    private final PlayerSubSkillDAO subSkillDAO;
    private final PlayerFormDAO formDAO;
    private final PlayerExpDAO expDAO;
    private final ManualSubSkillDAO manualDAO;
    private final PlayerInfoMapper mapper;

    private record Skill(ToDoubleFunction<PlayerSubSkill> get, ObjDoubleConsumer<PlayerSubSkill> set) {}
    private static final Map<String, Skill> SKILLS = Map.of(
        "stamina", new Skill(PlayerSubSkill::getStamina, PlayerSubSkill::setStamina),
        "keeper", new Skill(PlayerSubSkill::getKeeper, PlayerSubSkill::setKeeper),
        "defender", new Skill(PlayerSubSkill::getDefender, PlayerSubSkill::setDefender),
        "playmaker", new Skill(PlayerSubSkill::getPlaymaker, PlayerSubSkill::setPlaymaker),
        "winger", new Skill(PlayerSubSkill::getWinger, PlayerSubSkill::setWinger),
        "passing", new Skill(PlayerSubSkill::getPassing, PlayerSubSkill::setPassing),
        "scorer", new Skill(PlayerSubSkill::getScorer, PlayerSubSkill::setScorer),
        "setPieces", new Skill(PlayerSubSkill::getSetPieces, PlayerSubSkill::setSetPieces));

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<PlayerInfo> save(String username, ManualSubSkillRequest request) {
        validate(request);
        User user = userDAO.get(username);
        Team team = teamDAO.getByUserId(user.getId()).stream()
            .filter(t -> t.getId() == request.teamId()).findFirst()
            .orElseThrow(() -> error(HttpStatus.FORBIDDEN, "Team does not belong to this user"));
        League league = leagueDAO.get(team.getLeagueId()).orElseThrow();
        String week = String.format("S%03dW%02d", request.season() - league.getSeasonOffset(), request.week());
        // Stable locking order serializes simultaneous manual edits, including missing subskill rows.
        Map<Integer, List<Change>> byPlayer = new TreeMap<>();
        request.changes().forEach(change -> byPlayer.computeIfAbsent(change.playerId(), id -> new ArrayList<>()).add(change));
        List<PlayerInfo> result = new ArrayList<>();
        for (var entry : byPlayer.entrySet()) {
            int id = entry.getKey();
            if (!manualDAO.lockPlayer(team.getId(), week, id)) {
                throw error(HttpStatus.BAD_REQUEST, "Player not found in the selected team and week");
            }
            PlayerData data = playerDataDAO.getPlayer(team.getId(), id).stream()
                .filter(p -> week.equals(p.getSeasonWeek())).findFirst().orElseThrow();
            PlayerSubSkill storedSub = subSkillDAO.getPlayer(team.getId(), id).stream()
                .filter(p -> week.equals(p.getSeasonWeek())).findFirst().orElse(null);
            PlayerSubSkill sub = storedSub != null ? storedSub
                : PlayerSubSkill.builder().id(id).teamId(team.getId()).seasonWeek(week).build();
            PlayerForm form = formDAO.getPlayer(team.getId(), id).stream()
                .filter(p -> week.equals(p.getSeasonWeek())).findFirst().orElse(null);
            PlayerExp exp = expDAO.getPlayer(team.getId(), id).stream()
                .filter(p -> week.equals(p.getSeasonWeek())).findFirst().orElse(null);
            boolean skillsChanged = false;
            for (Change change : entry.getValue()) {
                double old = SKILLS.containsKey(change.skill()) ? SKILLS.get(change.skill()).get().applyAsDouble(sub)
                    : change.skill().equals("form") ? visibleFormSublevel(data.getPlayerForm(), form)
                    : (exp == null ? 0 : exp.getExp() - data.getExperience());
                if (Math.abs(old - change.oldValue()) > 0.0000001) {
                    throw error(HttpStatus.CONFLICT, "Sublevel changed since editing started; reload before saving");
                }
                if ((change.skill().equals("stamina") && data.getStaminaSkill() >= 9
                    || change.skill().equals("form") && data.getPlayerForm() >= 8) && change.newValue() != 0) {
                    throw error(HttpStatus.BAD_REQUEST, "Sublevel exceeds maximum skill level");
                }
                if (Math.abs(old - change.newValue()) < 0.0000001) continue;
                if (SKILLS.containsKey(change.skill())) {
                    SKILLS.get(change.skill()).set().accept(sub, change.newValue());
                    skillsChanged = true;
                } else if (change.skill().equals("form")) {
                    double value = data.getPlayerForm() + change.newValue();
                    manualDAO.saveForm(team.getId(), week, id, value);
                    if (form == null) form = PlayerForm.builder().id(id).teamId(team.getId()).seasonWeek(week)
                        .hiddenForm(value).expectedForm(value).build();
                    form.setForm(value);
                } else {
                    double value = data.getExperience() + change.newValue();
                    manualDAO.saveExperience(team.getId(), week, id, value);
                    exp = PlayerExp.builder().id(id).teamId(team.getId()).seasonWeek(week).exp(value).build();
                }
                manualDAO.recordChange(user.getId(), team.getId(), week, id, change.skill(), old, change.newValue());
            }
            if (skillsChanged) {
                HTMS htms = HTMSUtils.calculateHTMS(data.getAge(), data.getAgeDays(),
                    data.getKeeperSkill() + sub.getKeeper(), data.getDefenderSkill() + sub.getDefender(),
                    data.getPlaymakerSkill() + sub.getPlaymaker(), data.getWingerSkill() + sub.getWinger(),
                    data.getPassingSkill() + sub.getPassing(), data.getScorerSkill() + sub.getScorer(),
                    data.getSetPiecesSkill() + sub.getSetPieces());
                sub.setHtms(htms.getHtms());
                sub.setHtms28(htms.getHtms28());
                manualDAO.saveSkills(sub);
            }
            Player player = playerDAO.getPlayer(id);
            result.add(mapper.toInfo(player, data, null, storedSub != null || skillsChanged ? sub : null, form, exp));
        }
        return result;
    }

    static double visibleFormSublevel(int level, PlayerForm form) {
        if (form == null || level >= 8 || form.getForm() <= level) return 0;
        return Math.floor(form.getForm()) == level ? form.getForm() - level : 0.99;
    }

    private void validate(ManualSubSkillRequest request) {
        if (request == null || request.teamId() == null || request.season() == null || request.season() < 1
            || request.week() == null || request.week() < 1 || request.week() > 16
            || request.changes() == null || request.changes().isEmpty() || request.changes().size() > 1000) {
            throw error(HttpStatus.BAD_REQUEST, "Invalid manual sublevel request");
        }
        Set<String> seen = new HashSet<>();
        for (Change change : request.changes()) {
            if (change == null || change.playerId() == null || change.playerId() <= 0 || change.skill() == null
                || !(SKILLS.containsKey(change.skill()) || Set.of("form", "experience").contains(change.skill()))
                || change.oldValue() == null || !Double.isFinite(change.oldValue())
                || change.newValue() == null || !Double.isFinite(change.newValue())
                || change.newValue() < 0 || change.newValue() >= 1
                || Math.abs(change.newValue() * 100 - Math.rint(change.newValue() * 100)) > 0.0000001
                || !seen.add(change.playerId() + ":" + change.skill())) {
                throw error(HttpStatus.BAD_REQUEST, "Invalid or duplicate sublevel; use values from 0.00 to 0.99");
            }
        }
    }

    private static ResponseStatusException error(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }
}
