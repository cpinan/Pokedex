package com.example.pokedex.pokemondetail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pokedex.R
import com.example.pokedex.data.remote.responses.Pokemon
import com.example.pokedex.data.remote.responses.Type
import com.example.pokedex.util.Resource
import com.example.pokedex.util.parseStatToAbbr
import com.example.pokedex.util.parseStatToColor
import com.example.pokedex.util.parseTypeToColor
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState
import java.lang.Math.round
import java.util.*
import kotlin.math.roundToInt

@Composable
fun PokemonDetailScreen(
    dominantColor: Color,
    pokemonName: String,
    navController: NavController,
    topPadding: Dp = 20.dp,
    pokemonImageSize: Dp = 200.dp,
    pokemonViewModel: PokemonDetailViewModel = hiltViewModel(),
) {
    val pokemonInfo = produceState<Resource<Pokemon>>(
        initialValue = Resource.Loading()
    ) {
        value = pokemonViewModel.pokemonInfo(pokemonName)
    }.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dominantColor)
            .padding(bottom = 16.dp)
    ) {

        PokemonDetailTopSection(
            navController = navController,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.2F)
                .align(Alignment.TopCenter)
        )

        PokemonDetailStateWrapper(
            pokemonInfo = pokemonInfo,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = topPadding + pokemonImageSize / 2F,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
                .shadow(10.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colors.surface)
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            loadingModifier = Modifier
                .size(100.dp)
                .align(Alignment.Center)
                .padding(
                    top = topPadding + pokemonImageSize / 2F,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (pokemonInfo is Resource.Success) {
                pokemonInfo?.data?.sprites?.let {
                    val painter = rememberCoilPainter(
                        request = it.front_default,
                        fadeIn = true,
                    )

                    when (painter.loadState) {
                        is ImageLoadState.Loading -> {
                            CircularProgressIndicator(
                                color = MaterialTheme.colors.primary,
                                modifier = Modifier
                                    .size(pokemonImageSize)
                            )
                        }
                        is ImageLoadState.Success -> {
                            Image(
                                painter = painter,
                                contentDescription = pokemonInfo.data.name,
                                modifier = Modifier
                                    .size(pokemonImageSize)
                                    .offset(y = topPadding)
                            )
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonDetailTopSection(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black, Color.Transparent)
                )
            )
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .offset(16.dp, 16.dp)
                .clickable {
                    navController.popBackStack()
                }
        )
    }
}

@Composable
fun PokemonDetailStateWrapper(
    pokemonInfo: Resource<Pokemon>,
    modifier: Modifier = Modifier,
    loadingModifier: Modifier = Modifier
) {
    when (pokemonInfo) {
        is Resource.Success -> {
            PokemonDetailSection(
                pokemonInfo = pokemonInfo.data!!,
                modifier = modifier.offset(y = (-20).dp),
            )
        }
        is Resource.Error -> {
            Text(text = pokemonInfo.message!!, color = Color.Red, modifier = modifier)
        }
        is Resource.Loading -> {
            CircularProgressIndicator(
                color = MaterialTheme.colors.primary,
                modifier = loadingModifier,
            )
        }
    }
}

@Composable
fun PokemonDetailSection(
    pokemonInfo: Pokemon,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .offset(y = 100.dp)
            .verticalScroll(scrollState)
    ) {

        Text(
            text = "#${pokemonInfo.id} ${pokemonInfo.name.capitalize(Locale.ROOT)}",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onSurface,
        )

        PokemonTypeSection(types = pokemonInfo.types)

        PokemonDetailDataSection(
            pokemonWeight = pokemonInfo.weight,
            pokemonHeight = pokemonInfo.height,
        )

        PokemonBaseStats(pokemonInfo = pokemonInfo)

    }
}

@Composable
fun PokemonTypeSection(types: List<Type>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        for (type in types) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1F)
                    .padding(horizontal = 8.dp)
                    .clip(CircleShape)
                    .background(parseTypeToColor(type = type))
                    .height(35.dp),
            ) {
                Text(
                    text = type.type.name.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.ROOT
                        ) else it.toString()
                    },
                    color = Color.White,
                    fontSize = 18.sp,
                )
            }
        }
    }
}

@Composable
fun PokemonDetailDataSection(
    pokemonWeight: Int,
    pokemonHeight: Int,
    sectionHeight: Dp = 80.dp,
) {
    val pokemonWeightInKg = remember {
        (pokemonWeight * 100F).roundToInt() / 1000F
    }

    val pokemonHeightInMeters = remember {
        (pokemonHeight * 100F).roundToInt() / 1000F
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        PokemonDetailDataItem(
            dataValue = pokemonWeightInKg, dataUnit = "kg",
            dataIcon = painterResource(
                id = R.drawable.ic_weight,
            ),
            modifier = Modifier.weight(1F),
        )

        Spacer(
            modifier = Modifier
                .size(1.dp, sectionHeight)
                .background(Color.LightGray),
        )

        PokemonDetailDataItem(
            dataValue = pokemonHeightInMeters, dataUnit = "m",
            dataIcon = painterResource(
                id = R.drawable.ic_height,
            ),
            modifier = Modifier.weight(1F),
        )
    }
}

@Composable
fun PokemonDetailDataItem(
    dataValue: Float,
    dataUnit: String,
    dataIcon: Painter,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Icon(
            painter = dataIcon,
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$dataValue$dataUnit",
            color = MaterialTheme.colors.onSurface,
        )
    }
}

@Composable
fun PokemonStat(
    stateName: String,
    stateValue: Int,
    statMaxValue: Int,
    statColor: Color,
    height: Dp = 28.dp,
    animationDuration: Int = 1000,
    animationDelay: Int = 0,
) {
    var animationPlayed by remember {
        mutableStateOf(false)
    }

    val currentPercent = animateFloatAsState(
        targetValue = if (animationPlayed) {
            stateValue / statMaxValue.toFloat()
        } else {
            0F
        },
        animationSpec = tween(animationDuration, animationDelay)
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(
                if (isSystemInDarkTheme()) {
                    Color(0XFF505050)
                } else {
                    Color.LightGray
                }
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(currentPercent.value)
                .clip(CircleShape)
                .background(statColor)
                .padding(horizontal = 8.dp)
        ) {
            Text(text = stateName, fontWeight = FontWeight.Bold)
            Text(
                text = (currentPercent.value * statMaxValue).toInt().toString(),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun PokemonBaseStats(
    pokemonInfo: Pokemon,
    animationDelayPerItem: Int = 100
) {
    val maxBaseStats = remember {
        pokemonInfo.stats.maxOf { it.base_stat }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Base Stats: ", fontSize = 20.sp, color = MaterialTheme.colors.onSurface)
        Spacer(modifier = Modifier.height(8.dp))

        for (i in pokemonInfo.stats.indices) {
            val stat = pokemonInfo.stats[i]
            PokemonStat(
                stateName = parseStatToAbbr(stat),
                stateValue = stat.base_stat,
                statMaxValue = maxBaseStats,
                statColor = parseStatToColor(stat),
                animationDelay = animationDelayPerItem * i,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}