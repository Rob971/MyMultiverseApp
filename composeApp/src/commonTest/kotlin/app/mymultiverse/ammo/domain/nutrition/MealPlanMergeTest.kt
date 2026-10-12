package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class MealPlanMergeTest {

    private val weekKey = "2026-W40"

    // -- helpers --

    private fun day(lunch: String = "", dinner: String = "") = DayMeals(lunch, dinner)

    private fun plan(vararg days: DayMeals = arrayOf()): WeeklyMealPlan {
        val list = List(WeeklyMealPlan.DAYS_IN_WEEK) { index -> days.getOrElse(index) { DayMeals() } }
        return WeeklyMealPlan(weekKey, list)
    }

    private fun edit(plan: WeeklyMealPlan, dayIndex: Int, lunch: String? = null, dinner: String? = null): WeeklyMealPlan {
        val days = plan.days.mapIndexed { index, d ->
            if (index == dayIndex) DayMeals(lunch ?: d.lunch, dinner ?: d.dinner) else d
        }
        return plan.copy(days = days)
    }

    // ---------------------------------------------------------------
    // 1. The three-frame rules (per cell)
    // ---------------------------------------------------------------

    @Test
    fun `local equals base — fast-forward to remote`() {
        val base = plan(day(lunch = "Soup"))
        val local = plan(day(lunch = "Soup"))
        val remote = plan(day(lunch = "Pasta"))

        val result = MealPlanMerge.merge(base, local, remote)

        assertEquals("Pasta", result.days[0].lunch)
    }

    @Test
    fun `remote equals base — local wins`() {
        val base = plan(day(lunch = "Soup"))
        val local = plan(day(lunch = "Pasta"))
        val remote = plan(day(lunch = "Soup"))

        val result = MealPlanMerge.merge(base, local, remote)

        assertEquals("Pasta", result.days[0].lunch)
    }

    @Test
    fun `both equal base — that value`() {
        val base = plan(day(lunch = "Soup"))
        val local = plan(day(lunch = "Soup"))
        val remote = plan(day(lunch = "Soup"))

        val result = MealPlanMerge.merge(base, local, remote)

        assertEquals("Soup", result.days[0].lunch)
    }

    @Test
    fun `both changed and differ — the pushing device local wins`() {
        val base = plan(day(lunch = "Soup"))
        val local = plan(day(lunch = "Pasta"))
        val remote = plan(day(lunch = "Risotto"))

        val result = MealPlanMerge.merge(base, local, remote)

        assertEquals("Pasta", result.days[0].lunch, "pushing device must win a same-cell conflict")
    }

    // ---------------------------------------------------------------
    // 2. Clearing a cell is a change like any other
    // ---------------------------------------------------------------

    @Test
    fun `local cleared, remote unchanged — the clear propagates`() {
        val base = plan(day(lunch = "Soup"))
        val local = plan(day(lunch = ""))
        val remote = plan(day(lunch = "Soup"))

        val result = MealPlanMerge.merge(base, local, remote)

        assertEquals("", result.days[0].lunch)
    }

    @Test
    fun `remote cleared, local unchanged — the clear propagates`() {
        val base = plan(day(lunch = "Soup"))
        val local = plan(day(lunch = "Soup"))
        val remote = plan(day(lunch = ""))

        val result = MealPlanMerge.merge(base, local, remote)

        assertEquals("", result.days[0].lunch, "an emptied cell must not be resurrected by the stale device")
    }

    @Test
    fun `both cleared — stays empty`() {
        val base = plan(day(lunch = "Soup"))
        val local = plan(day(lunch = ""))
        val remote = plan(day(lunch = ""))

        val result = MealPlanMerge.merge(base, local, remote)

        assertEquals("", result.days[0].lunch)
    }

// ---------------------------------------------------------------
    // 3. base == null (never synced): union by cell
    // ---------------------------------------------------------------

    @Test
    fun `null base — non-empty local cell wins over empty remote cell`() {
        val local = plan(day(lunch = "Pasta"))
        val remote = plan()

        val result = MealPlanMerge.merge(null, local, remote)

        assertEquals("Pasta", result.days[0].lunch)
    }

    @Test
    fun `null base — empty local cell takes remote`() {
        val local = plan()
        val remote = plan(day(lunch = "Pasta"))

        val result = MealPlanMerge.merge(null, local, remote)

        assertEquals("Pasta", result.days[0].lunch)
    }

    @Test
    fun `null base — both non-empty and differ — remote wins`() {
        val local = plan(day(lunch = "Pasta"))
        val remote = plan(day(lunch = "Risotto"))

        val result = MealPlanMerge.merge(null, local, remote)

        assertEquals("Risotto", result.days[0].lunch, "no anchor to resolve a same-cell tie on a never-synced device")
    }

    @Test
    fun `null base — union preserves each side's own cells`() {
        val local = plan(day(lunch = "Pasta"))
        val remote = plan(day(), day(), day(), day(dinner = "Soup"))

        val result = MealPlanMerge.merge(null, local, remote)

        assertEquals("Pasta", result.days[0].lunch)
        assertEquals("Soup", result.days[3].dinner)
    }

    // ---------------------------------------------------------------
    // 4. Cell independence: 14 independent cells
    // ---------------------------------------------------------------

    @Test
    fun `different cells on different days merge independently`() {
        val base = plan(day(lunch = "Soup"), day(), day(lunch = "Salad"))
        val local = plan(day(lunch = "Soup"), day(), day(lunch = "Salad"), day(), day(dinner = "Pizza"))
        val remote = plan(day(lunch = "Pasta"), day(), day(lunch = "Salad"))

        val result = MealPlanMerge.merge(base, local, remote)

        // day0 lunch: base==local -> remote Pasta
        // day4 dinner: remote==base -> local Pizza
        assertEquals("Pasta", result.days[0].lunch)
        assertEquals("Pizza", result.days[4].dinner)
        assertEquals("Salad", result.days[2].lunch)
    }

    @Test
    fun `lunch and dinner of the same day resolve independently`() {
        val base = plan(day(lunch = "Soup", dinner = "Fish"))
        val local = plan(day(lunch = "Soup", dinner = "Pizza"))
        val remote = plan(day(lunch = "Pasta", dinner = "Fish"))

        val result = MealPlanMerge.merge(base, local, remote)

        assertEquals("Pasta", result.days[0].lunch)
        assertEquals("Pizza", result.days[0].dinner)
    }

    // ---------------------------------------------------------------
    // 5. Malformed inputs: return local unchanged, never throw
    // ---------------------------------------------------------------

    @Test
    fun `week key mismatch — local returned unchanged`() {
        val local = plan(day(lunch = "Pasta"))
        val remote = WeeklyMealPlan(weekKey = "2026-W41", days = List(7) { DayMeals() })

        val result = MealPlanMerge.merge(null, local, remote)

        assertSame(local, result, "mismatched week keys must return local unchanged")
        assertEquals("Pasta", result.days[0].lunch)
    }

    @Test
    fun `remote with six days — local returned unchanged`() {
        val local = plan(day(lunch = "Pasta"))
        val remote = WeeklyMealPlan(weekKey = weekKey, days = List(6) { DayMeals() })

        val result = MealPlanMerge.merge(null, local, remote)

        assertSame(local, result)
    }

    @Test
    fun `local with six days — local returned unchanged`() {
        val local = WeeklyMealPlan(weekKey = weekKey, days = List(6) { DayMeals() })
        val remote = plan(day(lunch = "Pasta"))

        val result = MealPlanMerge.merge(null, local, remote)

        assertSame(local, result)
    }

    @Test
    fun `malformed base — local returned unchanged`() {
        val local = plan(day(lunch = "Pasta"))
        val remote = plan()
        val base = WeeklyMealPlan(weekKey = "2026-W41", days = List(6) { DayMeals() })

        val result = MealPlanMerge.merge(base, local, remote)

        assertSame(local, result)
    }

    // ---------------------------------------------------------------
    // 6. Regression: the S5 incident shape, cell level
    // ---------------------------------------------------------------

    @Test
    fun `incident regression — partner's Monday edit survives a stale Friday push`() {
        val base = plan(day(lunch = "Soup"))
        // Stale device B still sees Monday "Soup" locally (base) and pushes a Friday change
        val local = edit(base, dayIndex = 4, dinner = "Pizza")
        // Partner A changed Monday lunch to Pasta on the server
        val remote = plan(day(lunch = "Pasta"))

        val result = MealPlanMerge.merge(base, local, remote)

        // Monday: base==local(Soup) -> remote Pasta (partner kept).
        // Friday: remote==base("") -> local Pizza (own offline edit kept).
        assertEquals("Pasta", result.days[0].lunch, "stale Monday soup must not clobber the partner's Pasta")
        assertEquals("Pizza", result.days[4].dinner)
    }
}