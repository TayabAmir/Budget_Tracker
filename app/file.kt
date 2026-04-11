fun main1() {

    print("Enter a number: ")
    val number = readLine()?.toIntOrNull() ?: 0

    if (number % 2 == 0 && number % 3 == 0) {
        println("Even & Multiple of 3")
    }
    else if (number % 2 == 0) {
        println("Even")
    }
    else if (number % 3 == 0) {
        println("Multiple of 3")
    }
    else {
        println("None")
    }
}

fun main2() {

    var a = 7
    println(a++)   // prints 7, then a becomes 8
    println(++a)   // a becomes 9, then prints 9
    println(a--)   // prints 9, then a becomes 8
    println(--a)   // a becomes 7, then prints 7
}

fun main3() {

    print("Enter marks: ")
    val marks = readLine()?.toIntOrNull() ?: 0

    when {
        marks < 0 || marks > 100 -> println("Invalid Marks")
        marks >= 90 -> println("Grade A")
        marks >= 80 -> println("Grade B")
        marks >= 70 -> println("Grade C")
        marks >= 60 -> println("Grade D")
        else -> println("Grade F")
    }
}

fun main4() {

    for (num in 1..200) {
        if (isPrime(num)) {
            println(num)
        }
    }
}

fun isPrime(number: Int): Boolean {

    if (number <= 1) {
        return false
    }

    for (i in 2 until number) {
        if (number % i == 0) {
            return false
        }
    }

    return true
}
fun main5() {

    val n = 5

    for (i in 1..n) {

        val stars = 2 * i - 1

        for (j in 1..stars) {
            print("*")
        }

        println()
    }
}
fun main6() {

    for (num in 1..100) {

        if ((num % 4 == 0 || num % 6 == 0) && num % 12 != 0) {
            println(num)
        }
    }
}
fun main7() {

    var number = 472
    var sum = 0

    while (number != 0) {

        val digit = number % 10
        sum = sum + digit
        number = number / 10
    }

    println("Sum of digits = $sum")
}
fun main8() {

    var number = 1234
    var reverse = 0

    while (number != 0) {

        val digit = number % 10
        reverse = reverse * 10 + digit
        number = number / 10
    }

    println("Reversed number = $reverse")
}
fun isPalindrome(text: String): Boolean {

    val cleanedText = text.replace(" ", "").lowercase()
    val reversedText = cleanedText.reversed()

    return cleanedText == reversedText
}
fun factorial(n: Int): Long {

    if (n < 0) {
        println("Negative numbers not allowed")
        return -1
    }

    var result: Long = 1

    for (i in 1..n) {
        result = result * i
    }

    return result
}
fun calculateDiscount(
    price: Double,
    discountPercent: Int,
    isMember: Boolean
): Double {
    if (price < 0 || discountPercent < 0) {
        return 0.0
    }
    var totalDiscount = discountPercent
    if (isMember) {
        totalDiscount = totalDiscount + 5
    }
    val discountAmount = price * totalDiscount / 100
    var finalPrice = price - discountAmount
    if (finalPrice < 0) {
        finalPrice = 0.0
    }
    return finalPrice
}
