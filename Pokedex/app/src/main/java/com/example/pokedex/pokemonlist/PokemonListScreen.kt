package com.example.pokedex.pokemonlist

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.Coil
import coil.ImageLoader
import coil.bitmap.BitmapPool
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Size
import coil.transform.CircleCropTransformation
import coil.transform.Transformation
import com.example.pokedex.R
import com.example.pokedex.SCREEN_POKEMON_DETAIL
import com.example.pokedex.data.models.PokedexListEntry
import com.example.pokedex.ui.theme.RobotoCondensed
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState
import kotlinx.coroutines.launch

@Composable
fun PokemonListScreen(
    navController: NavController,
    viewModel: PokemonListViewModel = hiltViewModel(),
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.pokemon_logo),
                contentDescription = "Pokemon",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )

            SearchBar(
                hint = "Search...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                viewModel.searchPokemonList(it)
            }

            Spacer(modifier = Modifier.height(16.dp))
            PokemonList(navController = navController)
        }
    }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String = "",
    onSearch: (String) -> Unit = {}
) {
    var text = remember {
        mutableStateOf("")
    }

    var isHintDisplayed = remember {
        mutableStateOf(hint != "")
    }

    Box(
        modifier = modifier
    ) {
        BasicTextField(
            value = text.value,
            onValueChange = {
                text.value = it
                onSearch(it)
            },
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(5.dp, CircleShape)
                .background(Color.White, CircleShape)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .onFocusChanged {
                    isHintDisplayed.value = !it.isFocused
                }
        )

        if (isHintDisplayed.value) {
            Text(
                text = hint,
                color = Color.LightGray,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }

}

@Composable
fun PokemonList(
    navController: NavController,
    viewModel: PokemonListViewModel = hiltViewModel(),
) {
    val pokemonList = remember { viewModel.pokemonList }
    val endReached = remember { viewModel.endReached }
    val loadError = remember { viewModel.loadError }
    val isLoading = remember { viewModel.isLoading }
    val isSearching = remember { viewModel.isSearching }

    LazyColumn(
        contentPadding = PaddingValues(16.dp)
    ) {
        val itemCount =
            if (pokemonList.value.size % 2 == 0) pokemonList.value.size / 2 else pokemonList.value.size / 2 + 1

        items(itemCount) {
            if (it >= itemCount - 1 && !endReached.value && !isLoading.value && !isSearching.value) {
                LaunchedEffect(key1 = true) {
                    viewModel.loadPokemonPaginated()
                }
            }
            PokedexRow(rowIndex = it, entries = pokemonList.value, navController = navController)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (isLoading.value) {
            CircularProgressIndicator(
                color = MaterialTheme.colors.primary
            )
        }
        if (loadError.value.isNotEmpty()) {
            RetrySection(error = loadError.value) {
                viewModel.loadPokemonPaginated()
            }
        }
    }
}

val colorMap: MutableMap<String, Color> = mutableMapOf()

@Composable
fun PokedexEntry(
    entry: PokedexListEntry,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = hiltViewModel(),
) {
    val defaultDominant = MaterialTheme.colors.surface
    var dominantColor = remember {
        mutableStateOf(colorMap[entry.pokemonName] ?: defaultDominant)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .shadow(5.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .aspectRatio(1F)
            .background(
                Brush.verticalGradient(
                    listOf(dominantColor.value, defaultDominant)
                )
            )
            .clickable {
                navController.navigate(
                    "$SCREEN_POKEMON_DETAIL/${dominantColor.value.toArgb()}/${entry.pokemonName}"
                )
            }
    ) {

        /*
        val request = ImageRequest.Builder(LocalContext.current)
            .data(entry.imageUrl)
            .target(
                onSuccess = {
                    viewModel.calculateDominantColor((it as BitmapDrawable).bitmap) { color ->
                        dominantColor = color
                    }
                }
            )
            .build()
        */

        val painter = rememberCoilPainter(
            request = ImageRequest.Builder(LocalContext.current)
                .data(entry.imageUrl)
                .build(),
            requestBuilder = {
                transformations(
                    object : Transformation {
                        override fun key(): String = "Default-Color-Transformation"

                        override suspend fun transform(
                            pool: BitmapPool,
                            input: Bitmap,
                            size: Size
                        ): Bitmap {
                            viewModel.calculateDominantColor(input) { color ->
                                // println("This color is $color for ${entry.pokemonName}")
                                dominantColor.value = color
                                colorMap[entry.pokemonName] = color
                            }
                            return input
                        }
                    })
            },
            fadeIn = true,
        )

        Column {
            when (painter.loadState) {
                is ImageLoadState.Loading -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier
                            .scale(0.5F)
                            .align(Alignment.CenterHorizontally),
                    )
                }
                is ImageLoadState.Success -> {
                    Image(
                        painter = painter,
                        contentDescription = entry.pokemonName,
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    /*
                    with(painter.request) {
                        if (this is ImageRequest) {

                        }
                    }*/
                }
                else -> {
                }
            }

            Text(
                text = entry.pokemonName,
                fontSize = 20.sp,
                fontFamily = RobotoCondensed,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PokedexRow(
    rowIndex: Int,
    entries: List<PokedexListEntry>,
    navController: NavController,
) {
    Column {
        Row {
            PokedexEntry(
                entry = entries[rowIndex * 2],
                navController = navController,
                modifier = Modifier.weight(1F)
            )
            Spacer(modifier = Modifier.width(16.dp))

            if (entries.size >= rowIndex * 2 + 2) {
                PokedexEntry(
                    entry = entries[rowIndex * 2 + 1],
                    navController = navController,
                    modifier = Modifier.weight(1F)
                )
            } else {
                Spacer(modifier = Modifier.weight(1F))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun RetrySection(
    error: String,
    onRetry: () -> Unit
) {
    Column {
        Text(text = error, color = Color.Red, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onRetry() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Retry")
        }
    }
}