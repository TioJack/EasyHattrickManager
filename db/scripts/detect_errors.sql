SELECT t.team_id
FROM training t
GROUP BY t.team_id
HAVING SUM(t.season_week = 'S092W02') = 0;
SELECT t.team_id
FROM player_data t
GROUP BY t.team_id
HAVING SUM(t.season_week = 'S092W02') = 0;
SELECT t.team_id
FROM trainer t
GROUP BY t.team_id
HAVING SUM(t.season_week = 'S092W02') = 0;
SELECT t.team_id
FROM staff_member t
GROUP BY t.team_id
HAVING SUM(t.season_week = 'S092W02') = 0;


SELECT t.team_id
FROM training t
GROUP BY t.team_id
HAVING SUM(t.season_week = 'S091W16') > 0
   AND SUM(t.season_week = 'S092W02') > 0
   AND SUM(t.season_week = 'S092W01') = 0;


SELECT t.team_id
FROM player_data t
GROUP BY t.team_id
HAVING SUM(t.season_week = 'S091W16') > 0
   AND SUM(t.season_week = 'S092W02') > 0
   AND SUM(t.season_week = 'S092W01') = 0;

SELECT t.team_id
FROM trainer t
GROUP BY t.team_id
HAVING SUM(t.season_week = 'S091W16') > 0
   AND SUM(t.season_week = 'S092W02') > 0
   AND SUM(t.season_week = 'S092W01') = 0;

SELECT t.team_id
FROM staff_member t
GROUP BY t.team_id
HAVING SUM(t.season_week = 'S091W16') > 0
   AND SUM(t.season_week = 'S092W02') > 0
   AND SUM(t.season_week = 'S092W01') = 0;


SELECT DISTINCT S1.user_id
FROM (SELECT uc.user_id,
             CAST(JSON_UNQUOTE(JSON_EXTRACT(uc.config, CONCAT('$.projects[', n.n, '].teamId'))) AS UNSIGNED)    AS team_id,
             CAST(JSON_UNQUOTE(JSON_EXTRACT(uc.config, CONCAT('$.projects[', n.n, '].iniSeason'))) AS UNSIGNED) AS iniSeason,
             CAST(JSON_UNQUOTE(JSON_EXTRACT(uc.config, CONCAT('$.projects[', n.n, '].iniWeek'))) AS UNSIGNED)   AS iniWeek
      FROM user_config uc
               JOIN (SELECT ones.i + tens.i * 10 AS n
                     FROM (SELECT 0 i
                           UNION ALL
                           SELECT 1
                           UNION ALL
                           SELECT 2
                           UNION ALL
                           SELECT 3
                           UNION ALL
                           SELECT 4
                           UNION ALL
                           SELECT 5
                           UNION ALL
                           SELECT 6
                           UNION ALL
                           SELECT 7
                           UNION ALL
                           SELECT 8
                           UNION ALL
                           SELECT 9) AS ones
                              CROSS JOIN
                          (SELECT 0 i
                           UNION ALL
                           SELECT 1
                           UNION ALL
                           SELECT 2
                           UNION ALL
                           SELECT 3
                           UNION ALL
                           SELECT 4
                           UNION ALL
                           SELECT 5
                           UNION ALL
                           SELECT 6
                           UNION ALL
                           SELECT 7
                           UNION ALL
                           SELECT 8
                           UNION ALL
                           SELECT 9) AS tens) AS n
                    ON n.n < JSON_LENGTH(JSON_EXTRACT(uc.config, '$.projects'))) AS S1
         JOIN team t ON t.id = S1.team_id AND t.user_id = S1.user_id
         JOIN league l ON l.id = t.league_id
WHERE NOT EXISTS (SELECT 1
                  FROM training tr
                  WHERE tr.team_id = S1.team_id
                    AND tr.season_week = CONCAT(
                          'S', LPAD(S1.iniSeason - CAST(l.seasonOffset AS SIGNED), 3, '0'),
                          'W', LPAD(S1.iniWeek, 2, '0')
                                         ));
