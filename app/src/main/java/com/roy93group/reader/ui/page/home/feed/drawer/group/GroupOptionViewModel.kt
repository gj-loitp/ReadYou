package com.roy93group.reader.ui.page.home.feed.drawer.group

import androidx.annotation.Keep
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roy93group.reader.domain.model.group.Group
import com.roy93group.reader.domain.service.RssService
import com.roy93group.reader.infrastructure.di.IODispatcher
import com.roy93group.reader.infrastructure.di.MainDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
class GroupOptionViewModel @Inject constructor(
    val rssService: RssService,
    @MainDispatcher
    private val mainDispatcher: CoroutineDispatcher,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _groupOptionUiState = MutableStateFlow(GroupOptionUiState())
    val groupOptionUiState: StateFlow<GroupOptionUiState> = _groupOptionUiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            rssService.get().pullGroups().collect { groups ->
                _groupOptionUiState.update { it.copy(groups = groups) }
            }
        }
    }

    fun showDrawer(scope: CoroutineScope, groupId: String) {
        scope.launch {
            _groupOptionUiState.update { it.copy(group = rssService.get().findGroupById(groupId)) }
            _groupOptionUiState.value.drawerState.show()
        }
    }

    fun hideDrawer(scope: CoroutineScope) {
        scope.launch { _groupOptionUiState.value.drawerState.hide() }
    }

    fun allAllowNotification(isNotification: Boolean, callback: () -> Unit = {}) {
        _groupOptionUiState.value.group?.let {
            viewModelScope.launch(ioDispatcher) {
                rssService.get().groupAllowNotification(it, isNotification)
                withContext(mainDispatcher) {
                    callback()
                }
            }
        }
    }

    fun showAllAllowNotificationDialog() {
        _groupOptionUiState.update { it.copy(allAllowNotificationDialogVisible = true) }
    }

    fun hideAllAllowNotificationDialog() {
        _groupOptionUiState.update { it.copy(allAllowNotificationDialogVisible = false) }
    }

    fun allParseFullContent(isFullContent: Boolean, callback: () -> Unit = {}) {
        _groupOptionUiState.value.group?.let {
            viewModelScope.launch(ioDispatcher) {
                rssService.get().groupParseFullContent(it, isFullContent)
                withContext(mainDispatcher) {
                    callback()
                }
            }
        }
    }

    fun showAllParseFullContentDialog() {
        _groupOptionUiState.update { it.copy(allParseFullContentDialogVisible = true) }
    }

    fun hideAllParseFullContentDialog() {
        _groupOptionUiState.update { it.copy(allParseFullContentDialogVisible = false) }
    }

    fun delete(callback: () -> Unit = {}) {
        _groupOptionUiState.value.group?.let {
            viewModelScope.launch(ioDispatcher) {
                rssService.get().deleteGroup(it)
                withContext(mainDispatcher) {
                    callback()
                }
            }
        }
    }

    fun showDeleteDialog() {
        _groupOptionUiState.update { it.copy(deleteDialogVisible = true) }
    }

    fun hideDeleteDialog() {
        _groupOptionUiState.update { it.copy(deleteDialogVisible = false) }
    }

    fun showClearDialog() {
        _groupOptionUiState.update { it.copy(clearDialogVisible = true) }
    }

    fun hideClearDialog() {
        _groupOptionUiState.update { it.copy(clearDialogVisible = false) }
    }

    fun clear(callback: () -> Unit = {}) {
        _groupOptionUiState.value.group?.let {
            viewModelScope.launch(ioDispatcher) {
                rssService.get().deleteArticles(group = it)
                withContext(mainDispatcher) {
                    callback()
                }
            }
        }
    }

    fun allMoveToGroup(callback: () -> Unit) {
        _groupOptionUiState.value.group?.let { group ->
            _groupOptionUiState.value.targetGroup?.let { targetGroup ->
                viewModelScope.launch(ioDispatcher) {
                    rssService.get().groupMoveToTargetGroup(group, targetGroup)
                    withContext(mainDispatcher) {
                        callback()
                    }
                }
            }
        }
    }

    fun showAllMoveToGroupDialog(targetGroup: Group) {
        _groupOptionUiState.update {
            it.copy(
                targetGroup = targetGroup,
                allMoveToGroupDialogVisible = true,
            )
        }
    }

    fun hideAllMoveToGroupDialog() {
        _groupOptionUiState.update {
            it.copy(
                targetGroup = null,
                allMoveToGroupDialogVisible = false,
            )
        }
    }

    fun rename() {
        _groupOptionUiState.value.group?.let {
            viewModelScope.launch {
                rssService.get().updateGroup(it.copy(name = _groupOptionUiState.value.newName))
                _groupOptionUiState.update { it.copy(renameDialogVisible = false) }
            }
        }
    }

    fun showRenameDialog() {
        _groupOptionUiState.update {
            it.copy(
                renameDialogVisible = true,
                newName = _groupOptionUiState.value.group?.name ?: "",
            )
        }
    }

    fun hideRenameDialog() {
        _groupOptionUiState.update {
            it.copy(
                renameDialogVisible = false,
                newName = "",
            )
        }
    }

    fun inputNewName(content: String) {
        _groupOptionUiState.update { it.copy(newName = content) }
    }
}

@Keep
@OptIn(ExperimentalMaterialApi::class)
data class GroupOptionUiState(
    var drawerState: ModalBottomSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden),
    val group: Group? = null,
    val targetGroup: Group? = null,
    val groups: List<Group> = emptyList(),
    val allAllowNotificationDialogVisible: Boolean = false,
    val allParseFullContentDialogVisible: Boolean = false,
    val allMoveToGroupDialogVisible: Boolean = false,
    val deleteDialogVisible: Boolean = false,
    val clearDialogVisible: Boolean = false,
    val newName: String = "",
    val renameDialogVisible: Boolean = false,
)