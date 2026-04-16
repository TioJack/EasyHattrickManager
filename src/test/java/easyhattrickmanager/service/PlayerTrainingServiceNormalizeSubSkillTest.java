package easyhattrickmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class PlayerTrainingServiceNormalizeSubSkillTest {

    private final PlayerTrainingService service = new PlayerTrainingService(null, null, null, null);

    @Test
    void shouldResetToZeroWhenLevelPopsWithoutOverflow() {
        assertEquals(0.0, normalizeSubSkill(10, 11, 0.83, 0.99), 1e-9);
    }

    @Test
    void shouldKeepOverflowWhenLevelPops() {
        assertEquals(0.07, normalizeSubSkill(10, 11, 0.83, 1.07), 1e-9);
    }

    @Test
    void shouldReconstructSubskillWhenLevelDrops() {
        assertEquals(0.88, normalizeSubSkill(11, 10, 0.10, -0.12), 1e-9);
    }

    @Test
    void shouldClampToZeroWhenDropIsTooLarge() {
        assertEquals(0.0, normalizeSubSkill(11, 10, 0.10, -1.20), 1e-9);
    }

    @Test
    void shouldCapAt099WhenSameLevelWrapsBackwards() {
        assertEquals(0.99, normalizeSubSkill(10, 10, 0.83, 1.07), 1e-9);
    }

    @Test
    void shouldWrapAndClampWhenSameLevelHasLargeOverflow() {
        assertEquals(0.35, normalizeSubSkill(10, 10, 0.20, 2.35), 1e-9);
    }

    private double normalizeSubSkill(int previousBaseSkill, int currentBaseSkill, double previousSubSkill, double value) {
        try {
            Method method = PlayerTrainingService.class.getDeclaredMethod("normalizeSubSkill", int.class, int.class, double.class, double.class);
            method.setAccessible(true);
            return (double) method.invoke(service, previousBaseSkill, currentBaseSkill, previousSubSkill, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
