package com.roy93group.reader.ui.page.setting.acc.addition

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.roy93group.reader.R
import com.roy93group.reader.domain.model.account.Account
import com.roy93group.reader.domain.model.account.AccountType
import com.roy93group.reader.ui.component.base.BaseDlg
import com.roy93group.reader.ui.component.base.BaseOutlineTextField
import com.roy93group.reader.ui.ext.collectAsStateValue
import com.roy93group.reader.ui.ext.showToast
import com.roy93group.reader.ui.page.common.RouteName
import com.roy93group.reader.ui.page.setting.acc.AccountViewModel

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun AddLocalAccountDialog(
    navController: NavHostController,
    viewModel: AdditionViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState = viewModel.additionUiState.collectAsStateValue()

    var name by remember { mutableStateOf("") }

    BaseDlg(
        modifier = Modifier.padding(horizontal = 44.dp),
        visible = uiState.addLocalAccountDialogVisible,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            focusManager.clearFocus()
            viewModel.hideAddLocalAccountDialog()
        },
        icon = {
            Icon(
                imageVector = Icons.Rounded.RssFeed,
                contentDescription = stringResource(R.string.local),
            )
        },
        title = {
            Text(
                text = stringResource(R.string.local),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                BaseOutlineTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.name),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    focusManager.clearFocus()
                    accountViewModel.addAccount(
                        Account(
                            type = AccountType.Local,
                            name = name,
                        )
                    ) {
                        if (it == null) {
                            context.showToast("Not valid credentials")
                        } else {
                            viewModel.hideAddLocalAccountDialog()
                            navController.popBackStack()
                            navController.navigate("${RouteName.ACCOUNT_DETAILS}/${it.id}") {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.hideAddLocalAccountDialog()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
