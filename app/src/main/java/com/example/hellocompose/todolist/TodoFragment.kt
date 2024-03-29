package com.example.hellocompose.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hellocompose.ui.theme.HelloComposeTheme

class TodoFragment : Fragment() {

    // 声明一ViewModel
    private lateinit var todoViewModel: TodoViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                HelloComposeTheme {
                    TodoScreen(todoViewModel)
                }
            }
        }
    }

    // 在onCreateView之后运行
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//    }

    // 在onCreateView之前运行
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        todoViewModel = ViewModelProvider(this)[TodoViewModel::class.java]
    }
}

@Composable
fun TodoScreen(todoViewModel: TodoViewModel) {
    Surface {
        TodoList(
            items = todoViewModel.todoItems,
            onAddItem = { todoViewModel.addItem(it) },
            onRemoveItem = { todoViewModel.removeItem(it) })
    }
}

@Composable
private fun TodoList(
    items: List<TodoItem>,
    onAddItem: (TodoItem) -> Unit,
    onRemoveItem: (TodoItem) -> Unit
) {
    Column {
        TodoItemInput(
            onClickAddItem = onAddItem
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(items) { item ->
                TodoRow(item) { onAddItem(item) }
            }
        }
        Button(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onClick = { if (items.isNotEmpty()) onRemoveItem(items.last()) }) {
            Text(
                fontSize = MaterialTheme.typography.body1.copy(fontSize = 24.sp).fontSize,
                text = "Remove the Last Item"
            )
        }
    }
}

@Composable
private fun TodoRow(
    item: TodoItem,
    modifier: Modifier = Modifier,
    onItemClicked: (TodoItem) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .clickable { onItemClicked(item) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            fontSize = MaterialTheme.typography.body1.copy(fontSize = 24.sp).fontSize,
            text = item.task
        )
        Icon(
            imageVector = item.icon.imageVector,
            contentDescription = stringResource(id = item.icon.contentDescription)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TodoItemInput(
    modifier: Modifier = Modifier,
    onClickAddItem: (TodoItem) -> Unit
) {
    val (text, setText) = remember { mutableStateOf("") }
    val (icon, setIcon) = remember { mutableStateOf(TodoIcon.Default) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val submit = {
        onClickAddItem(TodoItem(task = text, icon = icon))
        setText("")
        keyboardController?.hide()
    }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                value = text,
                onValueChange = { setText(it) },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                keyboardActions = KeyboardActions(onDone = {
                    submit()
                }),
            )
            Button(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp)),
                enabled = text.isNotBlank(),
                onClick = { submit() }
            ) {
                Text(text = "Add")
            }
        }
        AnimatedIconRow(
            currentIcon = icon,
            onIconChange = { setIcon(it) }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedIconRow(
    currentIcon: TodoIcon,
    onIconChange: (TodoIcon) -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    // remember these specs so they don't restart if recomposing during the animation
    // this is required since TweenSpec restarts on interruption
    val enter = remember { fadeIn(animationSpec = TweenSpec(300, easing = FastOutLinearInEasing)) }
    val exit = remember { fadeOut(animationSpec = TweenSpec(100, easing = FastOutSlowInEasing)) }

    Box(
        modifier = modifier.defaultMinSize(minHeight = 16.dp)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = enter,
            exit = exit,
        ) {
            Row {
                TodoIcon.values().forEach { icon ->

                    val isSelected = icon == currentIcon

                    val tint =
                        if (isSelected) MaterialTheme.colors.onSurface
                        else MaterialTheme.colors.onSurface.copy(alpha = 0.6f)

                    IconButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onIconChange(icon)
                        }) {
                        Column {
                            Icon(
                                imageVector = icon.imageVector,
                                contentDescription = stringResource(id = icon.contentDescription),
                                tint = tint
                            )
                            if (isSelected) {
                                Box(
                                    Modifier
                                        .padding(top = 3.dp)
                                        .width(icon.imageVector.defaultWidth)
                                        .height(1.dp)
                                        .background(tint)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoScreenPreview() {
    val items = listOf(
        TodoItem("Learn compose", TodoIcon.Event),
        TodoItem("Take the codelab"),
        TodoItem("Apply state", TodoIcon.Done),
        TodoItem("Build dynamic UIs", TodoIcon.Square)
    )
    TodoList(items, {}, {})
}

@Preview(showBackground = true)
@Composable
fun TodoItemInputPreview() {
    TodoItemInput(onClickAddItem = { TodoItem("Test") })
}
