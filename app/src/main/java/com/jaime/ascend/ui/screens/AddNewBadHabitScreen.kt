package com.jaime.ascend.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.jaime.ascend.data.repository.CategoryRepository
import com.jaime.ascend.data.repository.GoodHabitRepository
import com.jaime.ascend.data.repository.TemplateRepository

@Composable
fun AddNewBadHabitScreen(
    navController: NavController,
    categoryRepository: CategoryRepository,
    habitRepository: GoodHabitRepository,
    templateRepository: TemplateRepository,
    auth: FirebaseAuth,
) {/*
    val viewModel: HabitsViewModel = viewModel(
        factory = HabitsViewModelFactory(
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
                        val r = AppScreens.AddingGoodHabitScreen.route
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
    }*/
}
