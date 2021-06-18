package com.example.pokedex

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.pokedex.pokemondetail.PokemonDetailScreen
import com.example.pokedex.pokemonlist.PokemonListScreen
import com.example.pokedex.ui.theme.PokedexTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

const val SCREEN_POKEMON_LIST = "pokemon_list_screen"
const val SCREEN_POKEMON_DETAIL = "pokemon_detail_screen"

const val PROPERTY_DOMINANT_COLOR = "dominantColor"
const val PROPERTY_POKEMON_NAME = "pokemonName"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokedexTheme {
                Pokedex()
            }
        }
    }
}

@Composable
fun Pokedex() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = SCREEN_POKEMON_LIST,
    ) {
        composable(SCREEN_POKEMON_LIST) {
            PokemonListScreen(navController = navController)
        }
        composable(
            "$SCREEN_POKEMON_DETAIL/{$PROPERTY_DOMINANT_COLOR}/{$PROPERTY_POKEMON_NAME}",
            arguments = listOf(
                navArgument("$PROPERTY_DOMINANT_COLOR") {
                    type = NavType.IntType
                },
                navArgument("$PROPERTY_POKEMON_NAME") {
                    type = NavType.StringType
                }
            )
        ) {
            val dominantColor = remember {
                val color = it.arguments?.getInt("$PROPERTY_DOMINANT_COLOR")
                color?.let { Color(it) } ?: Color.White
            }

            val pokemonName = remember {
                it.arguments?.getString("$PROPERTY_POKEMON_NAME")
            }

            PokemonDetailScreen(
                dominantColor = dominantColor,
                pokemonName = pokemonName?.lowercase(Locale.ROOT) ?: "",
                navController = navController,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PokedexTheme {
    }
}