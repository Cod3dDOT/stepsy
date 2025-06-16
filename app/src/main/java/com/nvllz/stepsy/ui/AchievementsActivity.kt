package com.nvllz.stepsy.ui

import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.widget.addTextChangedListener
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.nvllz.stepsy.R
import com.nvllz.stepsy.util.AppPreferences
import com.nvllz.stepsy.util.Database
import com.nvllz.stepsy.util.Util.DistanceUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.nvllz.stepsy.util.AchievementsCacheUtil

class AchievementsActivity : AppCompatActivity() {
    private lateinit var database: Database
    private lateinit var dateFormat: DateFormat
    private lateinit var monthFormat: DateFormat
    private lateinit var displayFormat: DateFormat
    private lateinit var milestonesAdapter: MilestonesAdapter

    data class MilestoneAchievement(val milestone: Int, val timestamp: Long)

    data class ComputedResults(
        val mostStepsDay: String,
        val mostWalkedMonth: String,
        val totalDistance: String,
        val milestones: List<MilestoneAchievement>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setBackgroundDrawable(ContextCompat.getColor(this@AchievementsActivity, R.color.colorBackground).toDrawable())
            elevation = 0f
        }

        database = Database.getInstance(this)

        lifecycleScope.launch {
            dateFormat = SimpleDateFormat(AppPreferences.dateFormatString, Locale.getDefault())
            monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            displayFormat = SimpleDateFormat("LLLL yyyy", Locale.getDefault())

            setupViews()
            initializePreferences()
            loadCachedResultsIfAny()
            updateAchievements()
        }
    }

    private fun setupViews() {
        val notificationSwitch = findViewById<MaterialSwitch>(R.id.notification_switch)
        var isUserInteraction = false

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                AppPreferences.dataStore.edit { preferences ->
                    preferences[AppPreferences.PreferenceKeys.DAILY_GOAL_NOTIFICATION] = isChecked
                }
            }

            val goalTargetInput = findViewById<TextInputEditText>(R.id.goal_target_input)
            goalTargetInput.isEnabled = isChecked

            if (isChecked && isUserInteraction) {
                goalTargetInput.requestFocus()
                goalTargetInput.setSelection(goalTargetInput.text?.length ?: 0)
                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                inputMethodManager.showSoftInput(goalTargetInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }
        }

        notificationSwitch.post { isUserInteraction = true }

        findViewById<TextInputEditText>(R.id.goal_target_input).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            keyListener = DigitsKeyListener.getInstance("0123456789")

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    saveGoalTargetIfValid(text.toString())
                }
            }

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveGoalTargetIfValid(text.toString())
                    clearFocus()
                    true
                } else {
                    false
                }
            }

            addTextChangedListener { text ->
                if (!text.isNullOrEmpty()) {
                    saveGoalTargetIfValid(text.toString())
                }
            }
        }

        milestonesAdapter = MilestonesAdapter()
        findViewById<RecyclerView>(R.id.milestones_recycler_view).apply {
            adapter = milestonesAdapter
            layoutManager = LinearLayoutManager(this@AchievementsActivity)
            isNestedScrollingEnabled = false
        }
    }

    private fun saveGoalTargetIfValid(text: String) {
        try {
            val target = text.toInt()
            if (target > 0) {
                lifecycleScope.launch {
                    AppPreferences.dataStore.edit { preferences ->
                        preferences[AppPreferences.PreferenceKeys.DAILY_GOAL_TARGET] = target
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun initializePreferences() {
        val notificationEnabled = AppPreferences.dailyGoalNotification
        findViewById<MaterialSwitch>(R.id.notification_switch).isChecked = notificationEnabled

        val dailyTarget = AppPreferences.dailyGoalTarget
        findViewById<TextInputEditText>(R.id.goal_target_input).setText(dailyTarget.toString())
        findViewById<TextInputEditText>(R.id.goal_target_input).isEnabled = notificationEnabled
    }

    private fun loadCachedResultsIfAny() {
        AchievementsCacheUtil.loadCachedResults(this)?.let { cached ->
            updatePersonalRecord(R.id.most_steps_day_value, cached.mostStepsDay)
            updatePersonalRecord(R.id.most_walked_month_value, cached.mostWalkedMonth)
            updatePersonalRecord(R.id.total_distance_value, cached.totalDistance)

            if (cached.milestones != null && cached.milestones.isNotEmpty()) {
                showMilestones(cached.milestones)
            } else {
                showNoMilestones()
            }
        }
    }

    private fun updateAchievements() {
        lifecycleScope.launch {
            try {
                val firstEntry = database.firstEntry
                val lastEntry = database.lastEntry

                if (firstEntry == 0L || lastEntry == 0L) {
                    updatePersonalRecord(R.id.most_steps_day_value, getString(R.string.no_data_available))
                    updatePersonalRecord(R.id.most_walked_month_value, getString(R.string.no_data_available))
                    updatePersonalRecord(R.id.total_distance_value, getString(R.string.no_data_available))
                    showNoMilestones()
                    return@launch
                }

                val results = withContext(Dispatchers.Default) {
                    computeAllResults(firstEntry, lastEntry)
                }

                AchievementsCacheUtil.saveCachedResults(this@AchievementsActivity, results)

                updatePersonalRecord(R.id.most_steps_day_value, results.mostStepsDay)
                updatePersonalRecord(R.id.most_walked_month_value, results.mostWalkedMonth)
                updatePersonalRecord(R.id.total_distance_value, results.totalDistance)

                if (results.milestones.isNotEmpty()) {
                    showMilestones(results.milestones)
                } else {
                    showNoMilestones()
                }

            } catch (_: Exception) {
                updatePersonalRecord(R.id.most_steps_day_value, getString(R.string.error_loading_data))
                updatePersonalRecord(R.id.most_walked_month_value, getString(R.string.error_loading_data))
                updatePersonalRecord(R.id.total_distance_value, getString(R.string.error_loading_data))
                showNoMilestones()
            }
        }
    }

    private fun computeAllResults(firstEntry: Long, lastEntry: Long): ComputedResults {
        val entries = database.getEntries(firstEntry, lastEntry)

        if (entries.isNullOrEmpty()) {
            val noData = getString(R.string.no_data_available)
            return ComputedResults(noData, noData, noData, emptyList())
        }

        var minStepsEntry = entries[0]
        var maxStepsEntry = entries[0]
        var totalSteps = 0
        val monthlySteps = mutableMapOf<String, Int>()
        val milestones = calculateMilestoneAchievementsOptimized(entries)

        for (entry in entries) {
            totalSteps += entry.steps

            if (entry.steps > maxStepsEntry.steps) {
                maxStepsEntry = entry
            }

            val monthKey = monthFormat.format(Date(entry.timestamp))
            monthlySteps[monthKey] = (monthlySteps[monthKey] ?: 0) + entry.steps
        }

        val mostStepsDay = "${formatStepsWithDistance(maxStepsEntry.steps)}\n${dateFormat.format(Date(maxStepsEntry.timestamp))}"

        val maxMonth = monthlySteps.maxByOrNull { it.value }
        val mostWalkedMonth = if (maxMonth != null) {
            val date = monthFormat.parse(maxMonth.key) ?: Date()
            "${formatStepsWithDistance(maxMonth.value)}\n${displayFormat.format(date)}"
        } else {
            getString(R.string.no_data_available)
        }

        val totalDistance = "${formatStepsWithDistance(totalSteps)}\n" +
                getString(R.string.since_date, dateFormat.format(Date(minStepsEntry.timestamp)))

        return ComputedResults(mostStepsDay, mostWalkedMonth, totalDistance, milestones)
    }

    private fun calculateMilestoneAchievementsOptimized(entries: List<Database.Entry>): List<MilestoneAchievement> {
        if (entries.isNullOrEmpty()) return emptyList()

        val milestoneTargets = listOf(
            10_000, 50_000, 100_000, 500_000, 1_000_000, 2_000_000, 3_000_000, 4_000_000, 5_000_000,
            6_000_000, 7_000_000, 8_000_000, 9_000_000, 10_000_000, 11_000_000, 12_000_000,
            13_000_000, 14_000_000, 15_000_000, 16_000_000, 17_000_000, 18_000_000, 19_000_000, 20_000_000
        ).sorted()

        val achievements = mutableListOf<MilestoneAchievement>()
        var cumulativeSteps = 0
        var nextMilestoneIndex = 0
        val sortedEntries = entries.sortedBy { it.timestamp }

        for (entry in sortedEntries) {
            cumulativeSteps += entry.steps

            while (nextMilestoneIndex < milestoneTargets.size &&
                cumulativeSteps >= milestoneTargets[nextMilestoneIndex]) {

                achievements.add(MilestoneAchievement(milestoneTargets[nextMilestoneIndex], entry.timestamp))
                nextMilestoneIndex++
            }

            if (nextMilestoneIndex >= milestoneTargets.size) break
        }

        return achievements
    }

    private fun formatStepsWithDistance(steps: Int): String {
        val distanceKm = steps * AppPreferences.stepLength / 100000f
        val formattedSteps = if (steps >= 10_000) {
            NumberFormat.getIntegerInstance().format(steps)
        } else {
            steps.toString()
        }

        val stepsPlural = resources.getQuantityString(
            R.plurals.steps_text,
            steps,
            formattedSteps
        )

        return if (AppPreferences.distanceUnit == DistanceUnit.METRIC) {
            "$stepsPlural / %.2f km".format(distanceKm)
        } else {
            val distanceMiles = distanceKm * 0.621371f
            "$stepsPlural / %.2f mi".format(distanceMiles)
        }
    }

    private fun updatePersonalRecord(viewId: Int, value: String) {
        findViewById<TextView>(viewId).text = value
    }

    private fun showMilestones(milestones: List<MilestoneAchievement>) {
        findViewById<RecyclerView>(R.id.milestones_recycler_view).visibility = View.VISIBLE
        findViewById<TextView>(R.id.no_milestones_text).visibility = View.GONE
        milestonesAdapter.updateMilestones(milestones)
    }

    private fun showNoMilestones() {
        findViewById<RecyclerView>(R.id.milestones_recycler_view).visibility = View.GONE
        findViewById<TextView>(R.id.no_milestones_text).visibility = View.VISIBLE
    }
}

class MilestonesAdapter : RecyclerView.Adapter<MilestonesAdapter.MilestoneViewHolder>() {
    private var milestones: List<AchievementsActivity.MilestoneAchievement> = emptyList()
    private var dateFormat: DateFormat = SimpleDateFormat(AppPreferences.dateFormatString, Locale.getDefault())

    fun updateMilestones(newMilestones: List<AchievementsActivity.MilestoneAchievement>) {
        val oldSize = milestones.size
        val newSize = newMilestones.size

        milestones = newMilestones

        when {
            oldSize == 0 -> notifyItemRangeInserted(0, newSize)
            oldSize > newSize -> notifyItemRangeRemoved(newSize, oldSize - newSize)
            oldSize < newSize -> notifyItemRangeInserted(oldSize, newSize - oldSize)
            else -> notifyItemRangeChanged(0, newSize)
        }
    }


    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MilestoneViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_milestone_achievement, parent, false)
        return MilestoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: MilestoneViewHolder, position: Int) {
        holder.bind(milestones[position])
    }

    override fun getItemCount() = milestones.size

    inner class MilestoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val badge: TextView = itemView.findViewById(R.id.achievement_badge)
        private val title: TextView = itemView.findViewById(R.id.achievement_title)
        private val date: TextView = itemView.findViewById(R.id.achievement_date)

        fun bind(milestone: AchievementsActivity.MilestoneAchievement) {
            badge.text = when {
                milestone.milestone >= 10_000_000 -> "👑"
                milestone.milestone >= 9_000_000 -> "🦄"
                milestone.milestone >= 8_000_000 -> "🐉"
                milestone.milestone >= 7_000_000 -> "💫"
                milestone.milestone >= 6_000_000 -> "🏆"
                milestone.milestone >= 5_000_000 -> "💎"
                milestone.milestone >= 4_000_000 -> "🛸"
                milestone.milestone >= 3_000_000 -> "🚀"
                milestone.milestone >= 2_000_000 -> "🥇"
                milestone.milestone >= 1_000_000 -> "🥈"
                milestone.milestone >= 500_000 -> "🥉"
                milestone.milestone >= 100_000 -> "🔥"
                milestone.milestone >= 50_000 -> "💪"
                milestone.milestone >= 10_000 -> "🎯"
                else -> "🎯"
            }

            title.text = formatMilestoneTitle(milestone.milestone)
            date.text = dateFormat.format(Date(milestone.timestamp))
        }

        private fun formatMilestoneTitle(steps: Int): String {
            val distanceKm = steps * AppPreferences.stepLength / 100000f
            val distancePart = if (AppPreferences.distanceUnit == DistanceUnit.METRIC) {
                "%.2f km".format(distanceKm)
            } else {
                "%.2f mi".format(distanceKm * 0.621371f)
            }

            return when {
                steps >= 1_000_000 -> {
                    val millions = steps / 1_000_000.0
                    if (millions == millions.toInt().toDouble()) {
                        itemView.context.getString(R.string.million_steps_with_distance, millions.toInt(), distancePart)
                    } else {
                        itemView.context.getString(R.string.million_steps_decimal_with_distance, millions, distancePart)
                    }
                }
                steps >= 1_000 -> {
                    val thousands = steps / 1_000
                    itemView.context.getString(R.string.thousand_steps_with_distance, thousands, distancePart)
                }
                else -> itemView.context.getString(R.string.steps_count_with_distance, steps, distancePart)
            }
        }
    }
}