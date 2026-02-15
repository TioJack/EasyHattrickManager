# Calificaciones v1 consolidada (fuente principal: foro + validación simulador)

## Objetivo
Definir una implementación única de calificaciones que priorice la precisión frente al simulador de alineaciones de Hattrick.

Regla de prioridad de fuentes:
1. Foro (ingeniería inversa del simulador) como verdad principal.
2. Tabla oficial de pesos relativos por posición/habilidad para repartir coeficientes.
3. Excel (`Calificaciones (2)`) y rama `add_ratings` como apoyo para cobertura de casos y estructura.

## Fórmula base (núcleo)
Para cada contribución individual:

`contribucion = (K * nivel_efectivo) ^ 1.196`

Donde:
- `K` depende de área + posición + rol + habilidad.
- `nivel_efectivo` es la habilidad tras aplicar forma, lealtad/corazón y ajustes de partido.

## Coeficientes K de referencia (100%)
Valores base del foro:
- Mediocampo (Jugadas de IM normal): `K = 0.31106`
- Defensa central (Defensa de CD normal): `K = 0.50811`
- Defensa lateral (Defensa de lateral defensivo): `K = 0.83926`
- Ataque central (Anotación de delantero normal): `K = 0.50833`
- Ataque lateral (Lateral de extremo ofensivo): `K = 0.61856`

Resto de combinaciones:
- `K_combinacion = K_base_area * peso_relativo_oficial`

## Habilidad efectiva
Para cada habilidad usada en el cálculo:

1. Habilidad nominal (`skill`).
2. Sumar lealtad/corazón:
   - Lealtad lineal: `L/19` (escala interna).
   - Canterano (corazón): bonificación según modelo actual de negocio (si se mantiene, documentar exacto).
3. Aplicar forma:
   - `Ef(F) = sqrt(F/7)` usando escala interna de forma.
4. Aplicar modificadores de partido:
   - sobrepoblación de línea
   - clima/especialidades
   - factor de resistencia a lo largo del partido
5. Resultado:
   - `nivel_efectivo = (skill + bonos) * Ef(F) * penalizadores_bonificadores`

## Construcción de cada área
1. Calcular contribución individual por jugador/skill aplicable:
   - `(K * nivel_efectivo)^1.196`
2. Sumar contribuciones de todos los jugadores que impactan en el área.
3. Añadir base del área:
   - `R = 3 + suma_contribuciones`
4. Mostrar rating visual:
   - redondeo hacia arriba y escala visual del juego (`/4`).

## Factores de contexto (v1)
Se aplican como multiplicadores de área después de sumar contribuciones individuales:
- Mediocampo: espíritu + actitud + factor local/derbi (si aplica en el escenario).
- Ataques: confianza.
- Estilo del entrenador / táctica: como multiplicadores separados y trazables.

Nota:
- En `add_ratings` existen aproximaciones (`^1.165`, `+0.75`, `0.147832`, `0.417779`, `0.0525`).
- En v1 consolidada no se usarán por defecto si contradicen el modelo del foro/simulador.
- Solo se mantendrán si una validación cuantitativa demuestra mejor ajuste.

## Contrato de implementación (backend)
Entrada mínima por jugador:
- habilidades nominales (GK, DEF, PM, WING, PASS, SCOR)
- forma interna
- lealtad y flag canterano
- resistencia
- especialidad
- posición + orden (normal/of/def/hacia banda)

Entrada de partido:
- espíritu
- confianza
- actitud (normal/PIC/MOTS)
- condición local/visitante/derbi
- táctica activa
- estilo entrenador
- clima (si se modela)

Salida:
- rating por área: LD, CD, RD, MF, LA, CA, RA
- desglose de contribución por jugador/área para depuración

## Trazabilidad y depuración
La implementación debe exponer (modo debug):
- `nivel_efectivo` por skill y jugador
- `K` aplicado por contribución
- contribución antes y después de factores de contexto
- rating bruto (`R`) y rating visual final

## Plan de validación (obligatorio)
1. Preparar una batería de alineaciones de referencia (mínimo 30 casos).
2. Reproducir mismas condiciones del simulador (espíritu, confianza, táctica, etc.).
3. Medir error absoluto medio por zona (`MAE`) y error máximo.
4. Criterio de aceptación inicial:
   - `MAE <= 0.15` en escala visual por zona
   - sin sesgo sistemático en MF ni ataques laterales
5. Si falla:
   - revisar primero mapeo de pesos relativos/K.
   - luego factores de contexto.
   - no tocar exponente 1.196 salvo evidencia fuerte.

## Decisiones explícitas v1
- Se adopta `1.196` (foro) como exponente principal.
- Se adoptan los 5 `K` base del foro como anclas.
- Se separa claramente:
  - contribución individual (núcleo físico)
  - factores de contexto de partido
  - transformación a rating visual

## Pendientes para v1.1
- Tabla completa de pesos relativos por posición/rol/habilidad importada a configuración estática.
- Cobertura específica de clima y eventos especiales.
- Ajuste de integración con módulo de planificador para optimización de XI.
