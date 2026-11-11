# Referencia del simulador de alineaciones

La reconstrucción toma como referencia la sección Alineación de la pestaña de Hattrick que proporcionó el usuario el 16 de octubre de 2026: campo, lista de jugadores, banquillo y modo prueba. No incluye las pestañas Cargar, Instrucciones del equipo, Lanzadores de penaltis ni Revisión.

## Evidencia

- REA `list_browser_targets`: `ev_e3ae69b82af1d3c0c319cd55b32f65afd22e35b3ee753a0112103535e231ae65`.
- REA `inspect_web_page`, con fuentes JavaScript: `ev_b6d61de920ec2779dceef852239a777d4a1e27857e71f70dda148d3f02be159e`.
- Análisis estático del JavaScript observado: `ev_97970499d7d687db017d69fc06f9fbf93f7143cb54b00e8f1edbfea3df69a8d4`.
- Versión observada de recursos: `v2628809`; módulos principales `chunk-CVX64BDR.js` (simulador) y `chunk-CRZDYLVB.js` (cliente API).
- Captura facilitada por el usuario, DOM, estilos computados y recursos cargados en su Chrome mediante CDP en el puerto 9222.

Las fuentes son los paquetes compilados que entrega el navegador, no el repositorio original ni el código del servidor. La captura de REA es parcial respecto a tráfico anterior a su conexión y recursos de otros orígenes.

## Correspondencia

El campo conserva las dimensiones originales de 650 × 340 píxeles CSS y sus transformaciones de perspectiva. El módulo completo mide 958 píxeles CSS; escala 1,25 reproduce la captura proporcionada. Los estilos están encapsulados en el componente Angular. Las fuentes, el estadio, los iconos y los gráficos del banquillo se sirven localmente desde `assets/lineup/hattrick`.

Las posiciones 100–113 representan las catorce casillas disponibles, con once jugadores como máximo. Las posiciones 200–206 representan suplentes principales; 207–213 representan sus segundas opciones. Una segunda opción debe ser un suplente principal de otra casilla.

Las órdenes individuales usan los códigos observados: 0 normal, 1 ofensivo, 2 defensivo, 3 hacia medio, 4 hacia lateral. Las combinaciones disponibles dependen de la posición.

## Límite del motor de calificaciones

El cliente original envía la alineación, el contexto y los jugadores personalizados a `match/getDetailedRatingPredictions/{matchId}`. El servidor devuelve calificaciones de sectores, jugadores, táctica y evolución temporal. La fórmula no está incluida en los módulos entregados a Chrome.

La interfaz EHM emplea su servicio local de calificaciones para estimar sectores y contexto. Sus resultados no constituyen una reproducción matemática del servidor de Hattrick. El backend local no modela el tiempo meteorológico ni proporciona estrellas individuales, nivel de táctica o evolución por minuto. No se deben presentar valores inventados como si fueran resultados originales.

Las categorías de posición que no estén definidas en EHM y la ordenación por posiciones/recomendaciones se estiman a partir de las habilidades disponibles. No reproducen las predicciones individuales que devuelve el servidor original.

La selección de «Nivel actual» requiere valores de espíritu y confianza que el conjunto de datos semanal de EHM no contiene; el servicio local usa su contexto disponible. Las plantillas de jugadores proceden de la semana seleccionada de EHM y pueden diferir de la plantilla actual de Hattrick.
