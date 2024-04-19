package com.example.colormixer

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.colormixer.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val colorIds = listOf(
        R.id.userColorBoxR,
        R.id.userColorBoxY,
        R.id.userColorBoxB,
        R.id.userColorBoxC,
        R.id.userColorBoxG,
        R.id.userColorBoxM,
        R.id.userColorBoxBlack,
        R.id.userColorBoxWhite
    )
    private val colorValues = MutableList(colorIds.size) { 0 }
    private var selectedColorIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        finding the ui elements by calling their ids
        val howButton = findViewById<Button>(R.id.howButton)
//        Setting up click listener to show instruction pop up if user clicks
        howButton.setOnClickListener {
            showInstructionPopUp()
        }

        val plusButton = findViewById<Button>(R.id.plusButton)
        val minusButton = findViewById<Button>(R.id.minusButton)
        val resetButton = findViewById<Button>(R.id.resetButton)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val progressBar = findViewById<ProgressBar>(R.id.colorMatchBar)
        val satisfactionImageView = findViewById(R.id.satifactionlvlemoji) as ImageView

        // Set up click listeners for each color view
        for (i in colorIds.indices) {
            findViewById<View>(colorIds[i]).setOnClickListener {
                selectedColorIndex = i
                updateUI()
            }
        }

        // Plus button click listener
        plusButton.setOnClickListener {
            if (selectedColorIndex != -1) {
                colorValues[selectedColorIndex]++
                // Update the progress bar
                updateProgressBar()
                updateUI()
                satisfactionface()
            }
        }

        // Minus button click listener
        minusButton.setOnClickListener {
            if (selectedColorIndex != -1 && colorValues[selectedColorIndex] > 0) {
                colorValues[selectedColorIndex]--
                // Update the progress bar
                updateProgressBar()
                updateUI()
                satisfactionface()
            }
        }


        // Reset button click listener
        resetButton.setOnClickListener {
            colorValues.fill(0)
            selectedColorIndex = -1
            updateUI()
            progressBar.progress = 0
            val userColorTextView = findViewById<TextView>(R.id.userColorTextView)
            userColorTextView.text = "Your Color: 0%"
            satisfactionImageView.setImageResource(R.drawable.happy)
        }

        submitButton.setOnClickListener {
            val currentPercentage = progressBar.progress
            val message = when {
                currentPercentage < 30 -> "You're far from the target color. Keep trying!"
                currentPercentage < 60 -> "You're making progress, but there's still room for improvement."
                currentPercentage < 80 -> "You're getting closer to the target color. Keep it up!"
                currentPercentage < 100 -> "You're almost there! Just a little more to go."
                else -> "Congratulations! You've reached the target color."
            }

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Target: $currentPercentage%")
                .setMessage(message)
                .setNegativeButton("Stay") { _, _ ->
                    satisfactionface()
                }
                .setPositiveButton("New Color") { _, _ ->
//                    resetting everything
                    colorValues.fill(0)
                    selectedColorIndex = -1
                    updateUI()
                    progressBar.progress = 0
                    val userColorTextView = findViewById<TextView>(R.id.userColorTextView)
                    userColorTextView.text = "Your Color: 0%"
                    satisfactionImageView.setImageResource(R.drawable.happy)

//                    Generating a new random color and update the target color
                    val rnd = Random
                    val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
                    binding.targetColorBox.setBackgroundColor(color)
                }
                .setCancelable(true)
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }

    }

    fun showInstructionPopUp(){
        val instructions = """
            
            1. Help people find a color they're thinking of
            
            2. Mix colors to get the closest color as possible
            
            3. Remember to have fun!
            
            """.trimIndent()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Instructions")
            .setMessage(instructions)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
    private fun updateUI() {
        val counterintuitive = findViewById<TextView>(R.id.colorunitcount)
        val userColorBox = findViewById<View>(R.id.userColorBox)
//        val selectedColorBox = findViewById<View>(colorIds[selectedColorIndex])

        // Update the color unit count text
        if (selectedColorIndex != -1) {
            counterintuitive.text = colorValues[selectedColorIndex].toString()
        } else {
            counterintuitive.text = ""
        }

        // Update the background color of the user color box
        val color = calculateMixedColor()
        userColorBox.setBackgroundColor(color)
    }

    // Function to calculate the mixed color based on color values
    private fun calculateMixedColor(): Int {
        var totalRed = 0
        var totalGreen = 0
        var totalBlue = 0

        for (i in colorIds.indices) {
            val color = getColorForIndex(i)
            val value = colorValues[i]
            totalRed += Color.red(color) * value
            totalGreen += Color.green(color) * value
            totalBlue += Color.blue(color) * value
        }

        val totalUnits = colorValues.sum()
        return if (totalUnits > 0) {
            Color.rgb(totalRed / totalUnits, totalGreen / totalUnits, totalBlue / totalUnits)
        } else {
            Color.WHITE // Default color if no units are selected
        }
    }

    // Function to update the progress bar
    private fun updateProgressBar() {
        val userColorBox = findViewById<View>(R.id.userColorBox)
        val targetColorBox = findViewById<View>(R.id.targetColorBox)
        val progressBar = findViewById<ProgressBar>(R.id.colorMatchBar)
        val userColor = (userColorBox.background as? ColorDrawable)?.color
        val targetColor = (targetColorBox.background as? ColorDrawable)?.color
        val matchPercentageNum = findViewById<TextView>(R.id.userColorTextView)

        if (userColor != null && targetColor != null) {
            val percentageMatch = getMatchPercentage(userColor, targetColor)
            progressBar.progress = percentageMatch.toInt()
            matchPercentageNum.text = String.format("Your Color: %.1f%%", percentageMatch)
        }
    }

    // Function to get the color for the selected color index
    private fun getColorForIndex(index: Int): Int {
        return when (index) {
            0 -> Color.RED
            1 -> Color.YELLOW
            2 -> Color.BLUE
            3 -> Color.CYAN
            4 -> Color.GREEN
            5 -> Color.MAGENTA
            6 -> Color.BLACK
            7 -> Color.WHITE
            else -> Color.WHITE
        }
    }

    // Function to calculate color match percentage
    private fun getMatchPercentage(userColor: Int, targetColor: Int): Double {
        val userRed = Color.red(userColor)
        val userGreen = Color.green(userColor)
        val userBlue = Color.blue(userColor)
        val targetRed = Color.red(targetColor)
        val targetGreen = Color.green(targetColor)
        val targetBlue = Color.blue(targetColor)

        val totalDiff = (Math.abs(userRed - targetRed) +
                Math.abs(userGreen - targetGreen) +
                Math.abs(userBlue - targetBlue)).toDouble()
        val maxDiff = 3 * 255.0
        return ((maxDiff - totalDiff) / maxDiff) * 100
    }
    fun satisfactionface() {
        val satisfactionImageView = findViewById(R.id.satifactionlvlemoji) as ImageView
        val progressBar = findViewById<ProgressBar>(R.id.colorMatchBar)
        when {
            progressBar.progress < 20 -> satisfactionImageView.setImageResource(R.drawable.cry)
            progressBar.progress < 40 -> satisfactionImageView.setImageResource(R.drawable.sad)
            progressBar.progress < 60 -> satisfactionImageView.setImageResource(R.drawable.neutralemoji)
            progressBar.progress < 80 -> satisfactionImageView.setImageResource(R.drawable.happy)
            else -> satisfactionImageView.setImageResource(R.drawable.excited)
        }
    }
}