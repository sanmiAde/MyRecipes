package com.sanmiade.myrecipes

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MyRecipesApplication

fun main(args: Array<String>) {
    runApplication<MyRecipesApplication>(*args)
}
