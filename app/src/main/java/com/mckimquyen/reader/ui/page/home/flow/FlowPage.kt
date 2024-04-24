package com.mckimquyen.reader.ui.page.home.flow

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.work.WorkInfo
import com.mckimquyen.reader.R
import com.mckimquyen.reader.domain.model.general.MarkAsReadConditions
import com.mckimquyen.reader.infrastructure.pref.LocalFlowArticleListDateStickyHeader
import com.mckimquyen.reader.infrastructure.pref.LocalFlowArticleListFeedIcon
import com.mckimquyen.reader.infrastructure.pref.LocalFlowArticleListTonalElevation
import com.mckimquyen.reader.infrastructure.pref.LocalFlowFilterBarFilled
import com.mckimquyen.reader.infrastructure.pref.LocalFlowFilterBarPadding
import com.mckimquyen.reader.infrastructure.pref.LocalFlowFilterBarStyle
import com.mckimquyen.reader.infrastructure.pref.LocalFlowFilterBarTonalElevation
import com.mckimquyen.reader.infrastructure.pref.LocalFlowTopBarTonalElevation
import com.mckimquyen.reader.ui.component.FilterBar
import com.mckimquyen.reader.ui.component.base.DisplayText
import com.mckimquyen.reader.ui.component.base.FeedbackIconButton
import com.mckimquyen.reader.ui.component.base.BaseExtensibleVisibility
import com.mckimquyen.reader.ui.component.base.BaseScaffold
import com.mckimquyen.reader.ui.component.base.SwipeRefresh
import com.mckimquyen.reader.ui.ext.collectAsStateValue
import com.mckimquyen.reader.ui.page.common.RouteName
import com.mckimquyen.reader.ui.page.home.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    com.google.accompanist.pager.ExperimentalPagerApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class,
)
@Composable
fun FlowPage(
    navController: NavHostController,
    flowViewModel: FlowViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val topBarTonalElevation = LocalFlowTopBarTonalElevation.current
    val articleListTonalElevation = LocalFlowArticleListTonalElevation.current
    val articleListFeedIcon = LocalFlowArticleListFeedIcon.current
    val articleListDateStickyHeader = LocalFlowArticleListDateStickyHeader.current
    val filterBarStyle = LocalFlowFilterBarStyle.current
    val filterBarFilled = LocalFlowFilterBarFilled.current
    val filterBarPadding = LocalFlowFilterBarPadding.current
    val filterBarTonalElevation = LocalFlowFilterBarTonalElevation.current
    val homeUiState = homeViewModel.homeUiState.collectAsStateValue()
    val flowUiState = flowViewModel.flowUiState.collectAsStateValue()
    val filterUiState = homeViewModel.filterUiState.collectAsStateValue()
    val pagingItems = homeUiState.pagingData.collectAsLazyPagingItems()
    val listState =
        if (pagingItems.itemCount > 0) flowUiState.listState else rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var markAsRead by remember { mutableStateOf(false) }
    var onSearch by remember { mutableStateOf(false) }
    val owner = LocalLifecycleOwner.current
    var isSyncing by remember { mutableStateOf(false) }
    homeViewModel.syncWorkLiveData.observe(owner) {
        it?.let { isSyncing = it.any { it.state == WorkInfo.State.RUNNING } }
    }

    LaunchedEffect(onSearch) {
        snapshotFlow { onSearch }.collect {
            if (it) {
                delay(100)  // ???
                focusRequester.requestFocus()
            } else {
                keyboardController?.hide()
                if (homeUiState.searchContent.isNotBlank()) {
                    homeViewModel.inputSearchContent("")
                }
            }
        }
    }

    LaunchedEffect(flowUiState.listState) {
        snapshotFlow { flowUiState.listState.firstVisibleItemIndex }.collect {
            if (it > 0) {
                keyboardController?.hide()
            }
        }
    }

    BackHandler(onSearch) {
        onSearch = false
    }

    BaseScaffold(
        topBarTonalElevation = topBarTonalElevation.value.dp,
        containerTonalElevation = articleListTonalElevation.value.dp,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface
            ) {
                onSearch = false
                if (navController.previousBackStackEntry == null) {
                    navController.navigate(RouteName.FEEDS) {
                        launchSingleTop = true
                    }
                } else {
                    navController.popBackStack()
                }
            }
        },
        actions = {
            BaseExtensibleVisibility(visible = !filterUiState.filter.isStarred()) {
                FeedbackIconButton(
                    imageVector = Icons.Rounded.DoneAll,
                    contentDescription = stringResource(R.string.mark_all_as_read),
                    tint = if (markAsRead) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                ) {
                    scope.launch {
                        // java.lang.NullPointerException: Attempt to invoke virtual method
                        // 'boolean androidx.compose.ui.node.LayoutNode.getNeedsOnPositionedDispatch$ui_release()'
                        // on a null object reference
                        if (flowUiState.listState.firstVisibleItemIndex != 0) {
                            flowUiState.listState.scrollToItem(0)
                        }
                        markAsRead = !markAsRead
                        onSearch = false
                    }
                }
            }
            FeedbackIconButton(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(R.string.search),
                tint = if (onSearch) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            ) {
                scope.launch {
                    // java.lang.NullPointerException: Attempt to invoke virtual method
                    // 'boolean androidx.compose.ui.node.LayoutNode.getNeedsOnPositionedDispatch$ui_release()'
                    // on a null object reference
                    if (flowUiState.listState.firstVisibleItemIndex != 0) {
                        flowUiState.listState.scrollToItem(0)
                    }
                    onSearch = !onSearch
                }
            }
        },
        content = {
            SwipeRefresh(
                onRefresh = {
                    if (!isSyncing) {
                        flowViewModel.sync()
                    }
                }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                ) {
                    item {
                        DisplayText(
                            modifier = Modifier.padding(start = if (articleListFeedIcon.value) 30.dp else 0.dp),
                            text = when {
                                filterUiState.group != null -> filterUiState.group.name
                                filterUiState.feed != null -> filterUiState.feed.name
                                else -> filterUiState.filter.toName()
                            },
                            desc = if (isSyncing) stringResource(R.string.syncing) else "",
                        )
                        BaseExtensibleVisibility(visible = markAsRead) {
                            Spacer(modifier = Modifier.height((56 + 24 + 10).dp))
                        }
                        MarkAsReadBar(
                            visible = markAsRead,
                            absoluteY = if (isSyncing) (4 + 16 + 180).dp else 180.dp,
                            onDismissRequest = {
                                markAsRead = false
                            },
                        ) {
                            markAsRead = false
                            flowViewModel.markAsRead(
                                groupId = filterUiState.group?.id,
                                feedId = filterUiState.feed?.id,
                                articleId = null,
                                conditions = it,
                            )
                        }
                        BaseExtensibleVisibility(visible = onSearch) {
                            SearchBar(
                                value = homeUiState.searchContent,
                                placeholder = when {
                                    filterUiState.group != null -> stringResource(
                                        R.string.search_for_in,
                                        filterUiState.filter.toName(),
                                        filterUiState.group.name
                                    )

                                    filterUiState.feed != null -> stringResource(
                                        R.string.search_for_in,
                                        filterUiState.filter.toName(),
                                        filterUiState.feed.name
                                    )

                                    else -> stringResource(
                                        R.string.search_for,
                                        filterUiState.filter.toName()
                                    )
                                },
                                focusRequester = focusRequester,
                                onValueChange = {
                                    homeViewModel.inputSearchContent(it)
                                },
                                onClose = {
                                    onSearch = false
                                    homeViewModel.inputSearchContent("")
                                }
                            )
                            Spacer(modifier = Modifier.height((56 + 24 + 10).dp))
                        }
                    }
                    ArticleList(
                        pagingItems = pagingItems,
                        isShowFeedIcon = articleListFeedIcon.value,
                        isShowStickyHeader = articleListDateStickyHeader.value,
                        articleListTonalElevation = articleListTonalElevation.value,
                        onClick = {
                            onSearch = false
                            navController.navigate("${RouteName.READING}/${it.article.id}") {
                                launchSingleTop = true
                            }
                        }
                    ) {
                        flowViewModel.markAsRead(
                            groupId = filterUiState.group?.id,
                            feedId = filterUiState.feed?.id,
                            articleId = it.article.id,
                            MarkAsReadConditions.All
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(128.dp))
                        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }
        },
        bottomBar = {
            FilterBar(
                filter = filterUiState.filter,
                filterBarStyle = filterBarStyle.value,
                filterBarFilled = filterBarFilled.value,
                filterBarPadding = filterBarPadding.dp,
                filterBarTonalElevation = filterBarTonalElevation.value.dp,
            ) {
                scope.launch {
                    // java.lang.NullPointerException: Attempt to invoke virtual method
                    // 'boolean androidx.compose.ui.node.LayoutNode.getNeedsOnPositionedDispatch$ui_release()'
                    // on a null object reference
                    if (flowUiState.listState.firstVisibleItemIndex != 0) {
                        flowUiState.listState.scrollToItem(0)
                    }
                }
                homeViewModel.changeFilter(filterUiState.copy(filter = it))
                homeViewModel.fetchArticles()
            }
        }
    )
}