package easyhattrickmanager.service;

import easyhattrickmanager.service.model.HTMS;
import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.dataresponse.PlayerSubSkillInfo;
import easyhattrickmanager.utils.HTMSUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomPlayerService {

    private final WageService wageService;
    private final TSIFormService tsiFormService;

    public PlayerInfo calculate(PlayerInfo player) {
        if (player.getId() >= 0 || player.getAge() < 17 || player.getAge() > 100
            || player.getAgeDays() < 0 || player.getAgeDays() > 111
            || player.getPlayerForm() < 0 || player.getPlayerForm() > 8) {
            throw new IllegalArgumentException("Invalid custom player age, form or identifier");
        }
        PlayerSubSkillInfo sub = player.getPlayerSubSkill();
        if (sub == null) {
            sub = PlayerSubSkillInfo.builder().build();
            player.setPlayerSubSkill(sub);
        }
        double stamina = skill(player.getStaminaSkill(), sub.getStamina(), 9.0);
        double keeper = skill(player.getKeeperSkill(), sub.getKeeper(), 20.99);
        double defender = skill(player.getDefenderSkill(), sub.getDefender(), 20.99);
        double playmaker = skill(player.getPlaymakerSkill(), sub.getPlaymaker(), 20.99);
        double winger = skill(player.getWingerSkill(), sub.getWinger(), 20.99);
        double passing = skill(player.getPassingSkill(), sub.getPassing(), 20.99);
        double scorer = skill(player.getScorerSkill(), sub.getScorer(), 20.99);
        double setPieces = skill(player.getSetPiecesSkill(), sub.getSetPieces(), 20.99);

        HTMS htms = HTMSUtils.calculateHTMS(player.getAge(), player.getAgeDays(), player.getKeeperSkill(),
            player.getDefenderSkill(), player.getPlaymakerSkill(), player.getWingerSkill(),
            player.getPassingSkill(), player.getScorerSkill(), player.getSetPiecesSkill());
        HTMS subHtms = HTMSUtils.calculateHTMS(player.getAge(), player.getAgeDays(), keeper,
            defender, playmaker, winger, passing, scorer, setPieces);
        player.setHtms(htms.getHtms());
        player.setHtms28(htms.getHtms28());
        sub.setHtms(subHtms.getHtms());
        sub.setHtms28(subHtms.getHtms28());
        player.setTSI((int) tsiFormService.calculateTSI(player.getAge(), player.getPlayerForm(), stamina,
            keeper, defender, playmaker, winger, passing, scorer));
        player.setSalary((int) wageService.calculateWage(player.getAge(), player.isAbroad(), player.getSpecialty(),
            keeper, defender, playmaker, winger, passing, scorer, setPieces));
        return player;
    }

    private double skill(int level, double subSkill, double max) {
        double value = level + subSkill;
        if (level < 0 || !Double.isFinite(subSkill) || subSkill < 0 || subSkill >= 1 || value > max) {
            throw new IllegalArgumentException("Invalid custom player skill");
        }
        return value;
    }
}
