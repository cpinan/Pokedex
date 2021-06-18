package com.example.pokedex.repository

import com.example.pokedex.data.remote.PokeApi
import com.example.pokedex.data.remote.responses.PokemonList
import com.example.pokedex.util.Resource
import dagger.hilt.android.scopes.ActivityScoped
import java.lang.Exception
import javax.inject.Inject

@ActivityScoped
class PokemonRepository @Inject constructor(
    private val api: PokeApi
) {
    suspend fun getPokemonList(
        limit: Int, offset: Int
    ): Resource<PokemonList> {
        val response = try {
            api.getPokemonList(limit, offset)
        } catch (e: Exception) {
            return Resource.Error(e?.message ?: "Failed to grab pokemon list")
        }
        return Resource.Success(response)
    }

    suspend fun getPokemonInfo(name: String) =
        try {
            val response = api.getPokemonInfo(name)
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e?.message ?: "Failed to grab pokemon $name")
        }
}