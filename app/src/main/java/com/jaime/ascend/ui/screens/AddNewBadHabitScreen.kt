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
import com.jaime.ascend.data.factory.BadHabitsViewModelFactory
import com.jaime.ascend.data.models.Category
import com.jaime.ascend.data.repository.BadHabitRepository
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.TemplateRepository
import com.jaime.ascend.ui.components.ActionBarWithBackButton
import com.jaime.ascend.ui.navigation.AppScreens
import com.jaime.ascend.utils.IconMapper.getCategoryIcon
import com.jaime.ascend.utils.IconMapper.getHabitIcon
import com.jaime.ascend.viewmodel.BadHabitsViewModel
import java.util.Locale

@Composable
fun AddNewBadHabitScreen(
    navController: NavController,
    categoryRepository: CategoryRepository,
    habitRepository: BadHabitRepository,
    templateRepository: TemplateRepository,
    auth: FirebaseAuth,
) {
    val viewModel: BadHabitsViewModel = viewModel(
        factory = BadHabitsViewModelFactory(
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
                screenName = stringResource(id = R.string.add_new_bad_habit_title),
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
                        val r = AppScreens.AddingBadHabitScreen.route
                            .replace("{templateId}", it.id)
                        navController.navigate(r)
                    }
                )
            }
        }
    }
}


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
