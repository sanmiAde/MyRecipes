package com.sanmiade.myrecipes

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class MyRecipesApplication {

}

fun main(args: Array<String>) {
    runApplication<MyRecipesApplication>(*args)
}

