package com.example.calculatrice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculatrice.ui.theme.CalculatriceTheme
import java.util.Stack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatriceTheme {
                CalculatorApp()
            }
        }
    }
}

@Composable
fun CalculatorApp() {
    var displayText by remember { mutableStateOf("0") }
    val historyList = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Historique des calculs
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.DarkGray)
                .padding(8.dp)
        ) {
            items(historyList) { historyItem ->
                Text(
                    text = historyItem,
                    fontSize = 20.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        // Ecran de calcul avec défilement horizontal pour les longs calculs
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
                .horizontalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = displayText,
                fontSize = 48.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Boutons de calculatrice
        val buttons = listOf(
            listOf("C", "⌫", "/", "*"),
            listOf("7", "8", "9", "-"),
            listOf("4", "5", "6", "+"),
            listOf("1", "2", "3", "="),
            listOf("0", ".", "(", ")")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { symbol ->
                    CalculatorButton(symbol, Modifier.weight(1f)) {
                        if (symbol == "=") {
                            val result = handleButtonPress(symbol, displayText)
                            historyList.add("$displayText = $result")
                            displayText = result
                        } else {
                            displayText = handleButtonPress(symbol, displayText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(symbol: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
    ) {
        Text(
            text = symbol,
            fontSize = 24.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

fun handleButtonPress(symbol: String, currentText: String): String {
    return when (symbol) {
        "C" -> "0"
        "⌫" -> if (currentText.length > 1) currentText.dropLast(1) else "0"
        "=" -> try {
            evaluateSimpleExpression(currentText).toString()
        } catch (e: Exception) {
            "Erreur"
        }
        else -> if (currentText == "0") symbol else currentText + symbol
    }
}

fun evaluateSimpleExpression(expression: String): Double {
    val operators = Stack<Char>()
    val values = Stack<Double>()
    var i = 0

    while (i < expression.length) {
        when (val ch = expression[i]) {
            in '0'..'9' -> {
                var num = 0.0
                while (i < expression.length && expression[i] in '0'..'9') {
                    num = num * 10 + (expression[i++] - '0')
                }
                if (i < expression.length && expression[i] == '.') {
                    var decimalPlace = 0.1
                    i++
                    while (i < expression.length && expression[i] in '0'..'9') {
                        num += (expression[i++] - '0') * decimalPlace
                        decimalPlace *= 0.1
                    }
                }
                values.push(num)
                i--
            }
            '+', '-', '*', '/' -> {
                while (operators.isNotEmpty() && precedence(operators.peek()) >= precedence(ch)) {
                    values.push(applyOp(operators.pop(), values.pop(), values.pop()))
                }
                operators.push(ch)
            }
        }
        i++
    }
    while (operators.isNotEmpty()) {
        values.push(applyOp(operators.pop(), values.pop(), values.pop()))
    }
    return values.pop()
}

fun precedence(op: Char): Int {
    return when (op) {
        '+', '-' -> 1
        '*', '/' -> 2
        else -> -1
    }
}

fun applyOp(op: Char, b: Double, a: Double): Double {
    return when (op) {
        '+' -> a + b
        '-' -> a - b
        '*' -> a * b
        '/' -> if (b != 0.0) a / b else Double.NaN
        else -> 0.0
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    CalculatriceTheme {
        CalculatorApp()
    }
}
