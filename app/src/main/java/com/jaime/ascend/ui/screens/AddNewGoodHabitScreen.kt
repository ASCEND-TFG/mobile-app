package com.jaime.ascend.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.jaime.ascend.R
import com.jaime.ascend.data.factory.GoodHabitsViewModelFactory
import com.jaime.ascend.data.models.Category
import com.jaime.ascend.data.models.HabitTemplate
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.HabitRepository
import com.jaime.ascend.data.repository.TemplateRepository
import com.jaime.ascend.ui.components.ActionBarWithBackButton
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.utils.IconMapper.getCategoryIcon
import com.jaime.ascend.utils.IconMapper.getHabitIcon
import com.jaime.ascend.viewmodel.GoodHabitsViewModel
import java.util.Locale

@Composable
fun AddNewGoodHabitScreen(
    navController: NavController,
    categoryRepository: CategoryRepository,
    habitRepository: HabitRepository,
    templateRepository: TemplateRepository,
    auth: FirebaseAuth,
) {
    val viewModel: GoodHabitsViewModel = viewModel(
        factory = GoodHabitsViewModelFactory(
            categoryRepository,
            habitRepository,
            templateRepository,
            auth
        )
    )
    val categories by viewModel.categories
    val isLoading by viewModel.isLoading
    val selectedCategory by viewModel.selectedCategory
    val templates by viewModel.templates

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ActionBarWithBackButton(
                screenName = stringResource(id = R.string.add_new_good_habit_title),
                navController = navController,
                modifier = Modifier,
                onBack = {
                    if (selectedCategory != null) {
                        viewModel.selectCategory(null)
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (categories.isEmpty()) {
                Text(text = "No categories found")
            } else if (selectedCategory == null) {
                CategoriesList(
                    categories = categories,
                    onCategorySelected = { viewModel.selectCategory(it) }
                )
            } else {
                TemplatesList(
                    templates = templates,
                    onTemplateSelected = {
                        val r = AppScreens.AddingGoodHabitScreen.route
                            .replace("{templateId}", it.id)
                        navController.navigate(r)
                    }
                )
            }
        }
    }
}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewGoodHabitScreen2(
    navController: NavController,
    categoryRepository: CategoryRepository,
    habitRepository: HabitRepository,
    auth: FirebaseAuth,
) {
    val viewModel: GoodHabitsViewModel = viewModel(
        factory = GoodHabitsViewModelFactory(categoryRepository, habitRepository, auth)
    )
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val focusManager = LocalFocusManager.current
    val currentLanguage = LocalConfiguration.current.locales[0].language

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ActionBarWithBackButton(
                screenName = stringResource(id = R.string.add_new_good_habit_title),
                navController = navController,
                modifier = Modifier
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is GoodHabitsViewModel.State.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is GoodHabitsViewModel.State.Error -> {
                    Text(
                        text = currentState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is GoodHabitsViewModel.State.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { query ->
                                viewModel.updateSearchQuery(query)
                            },
                            active = searchQuery.isNotEmpty(),
                            onActiveChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(top = 8.dp),
                            colors = SearchBarDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                            tonalElevation = 0.2.dp,
                            windowInsets = WindowInsets(0.dp),
                            placeholder = { Text(text = stringResource(R.string.search_hint)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            viewModel.updateSearchQuery("")
                                            focusManager.clearFocus()
                                        }
                                    ) {
                                        Icon(Icons.Filled.Close, "Clear")
                                    }
                                }
                            },
                            onSearch = { }
                        ) {
                            SearchResultsList(
                                habits = currentState.searchedHabits,
                                onHabitSelected = { habit ->
                                    val r = AppScreens.AddingGoodHabitScreen.route
                                        .replace("{categoryId}", currentState.currentCategory!!)
                                        .replace("{templateId}", "???")
                                    navController.navigate(r)
                                }
                            )
                        }

                        if (searchQuery.isEmpty()) {
                            if (currentState.currentCategory == null) {
                                CategoriesList(
                                    categories = currentState.categories,
                                    onCategorySelected = { categoryId ->
                                        viewModel.loadGoodHabitsByCategory(categoryId)
                                        focusManager.clearFocus()
                                    }
                                )
                            } else {
                                GoodHabitsList(
                                    habits = currentState.goodHabits,
                                    onBack = { viewModel.clearCurrentCategory() },
                                    onHabitSelected = { habit ->
                                        val r = AppScreens.AddingGoodHabitScreen.route
                                            .replace("{categoryId}", currentState.currentCategory)
                                            .replace("{templateId}", "???")
                                        navController.navigate(r)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun SearchResultsList(
    habits: List<GoodHabit>,
    onHabitSelected: (GoodHabit) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(habits) { habit ->
            GoodHabitCard(
                habit = habit,
                onClick = { onHabitSelected(habit) }
            )
        }
    }
}
*/
@Composable
private fun CategoriesList(
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection()),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(categories) { category ->
            ItemDisplayCard(
                item = DisplayItem.CategoryItem(category),
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun TemplatesList(
    templates: List<HabitTemplate>,
    onTemplateSelected: (HabitTemplate) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection()),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(templates) { template ->
            ItemDisplayCard(
                item = DisplayItem.TemplateItem(template),
                onClick = { onTemplateSelected(template) }
            )
        }
    }
}


@Composable
private fun ItemDisplayCard(
    item: DisplayItem,
    onClick: () -> Unit,
) {
    val (name, description, icon) = when (item) {
        is DisplayItem.CategoryItem -> Triple(
            item.category.getName(Locale.getDefault()),
            item.category.getDescription(Locale.getDefault()),
            getCategoryIcon(item.category.icon)
        )

        is DisplayItem.TemplateItem -> Triple(
            item.template.getName(Locale.getDefault()),
            item.template.getDescription(Locale.getDefault()),
            getHabitIcon(item.template.icon)
        )
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

sealed class DisplayItem {
    data class CategoryItem(val category: Category) : DisplayItem()
    data class TemplateItem(val template: HabitTemplate) : DisplayItem()
}