package com.example.nammareshme.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nammareshme.R
import com.example.nammareshme.ui.viewmodel.AppViewModel
import com.example.nammareshme.ui.viewmodel.AuthState
import com.example.nammareshme.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun AuthScreen(
    appViewModel: AppViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    onForgotPassword: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val scale = (screenWidth.value / 400f).coerceIn(0.85f, 1.1f)
    val context = LocalContext.current

    var isLoginMode by remember { mutableStateOf(true) }
    val currentLanguage by appViewModel.currentLanguage.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    // Form States
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Validation States
    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    // Google Sign In Configuration
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { token ->
                    authViewModel.signInWithGoogle(token)
                }
            } catch (e: ApiException) {
                // Translated toast
            }
        }
    }

    val regSuccessMsg = stringResource(R.string.registration_success_msg)
    LaunchedEffect(authState) {
        val current = authState
        when (current) {
            is AuthState.Authenticated -> onLoginSuccess()
            is AuthState.RegistrationSuccess -> {
                Toast.makeText(context, regSuccessMsg, Toast.LENGTH_LONG).show()
                isLoginMode = true
                authViewModel.resetState()
            }
            is AuthState.Error -> {
                Toast.makeText(context, current.message, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }
            is AuthState.Success -> {
                Toast.makeText(context, current.message, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // --- PREMIUM BACKGROUND ---
        Image(
            painter = painterResource(id = R.drawable.auth_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.01f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.02f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = (24 * scale).dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height((16 * scale).dp))

            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size((40 * scale).dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color(0xFF1B4332),
                        modifier = Modifier.size((20 * scale).dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier.size((34 * scale).dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.app_title),
                            color = Color(0xFF1B4332),
                            fontSize = (15 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                        Text(
                            text = stringResource(R.string.app_subtitle),
                            color = Color(0xFF2D6A4F),
                            fontSize = (9 * scale).sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier.height((32 * scale).dp).width((96 * scale).dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        LanguageSwitchItem(
                            text = if (currentLanguage == "English") "EN" else "ಕ",
                            isSelected = true,
                            onClick = { 
                                if (currentLanguage == "English") appViewModel.setLanguage("Kannada")
                                else appViewModel.setLanguage("English")
                            },
                            modifier = Modifier.fillMaxSize(),
                            scale = scale
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height((40 * scale).dp))

            // --- WELCOME SECTION ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isLoginMode) stringResource(R.string.welcome_back) else stringResource(R.string.create_account),
                    fontSize = (26 * scale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B4332),
                    lineHeight = (32 * scale).sp
                )
                Text(
                    text = if (isLoginMode) stringResource(R.string.login_continue) else stringResource(R.string.signup_start),
                    fontSize = (14 * scale).sp,
                    color = Color(0xFF1B4332).copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height((32 * scale).dp))

            // --- LOGIN/REGISTER TOGGLE ---
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = (50 * scale).dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    AuthToggleTab(
                        text = stringResource(R.string.login),
                        isSelected = isLoginMode,
                        onClick = { 
                            isLoginMode = true 
                            nameError = false
                            phoneError = false
                            passwordError = false
                        },
                        modifier = Modifier.weight(1f),
                        scale = scale
                    )
                    AuthToggleTab(
                        text = stringResource(R.string.register),
                        isSelected = !isLoginMode,
                        onClick = { 
                            isLoginMode = false 
                            nameError = false
                            phoneError = false
                            passwordError = false
                        },
                        modifier = Modifier.weight(1f),
                        scale = scale
                    )
                }
            }

            Spacer(modifier = Modifier.height((24 * scale).dp))

            // --- INPUT FIELDS ---
            Column(verticalArrangement = Arrangement.spacedBy((12 * scale).dp)) {
                AnimatedVisibility(
                    visible = !isLoginMode,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    AuthFieldCompact(
                        value = name,
                        onValueChange = { 
                            name = it
                            nameError = it.isBlank()
                        },
                        placeholder = stringResource(R.string.enter_name),
                        icon = Icons.Default.Person,
                        isError = nameError,
                        scale = scale
                    )
                }

                AuthFieldCompact(
                    value = phone,
                    onValueChange = { 
                        phone = it
                        phoneError = it.isBlank()
                    },
                    placeholder = stringResource(R.string.enter_phone),
                    icon = Icons.Default.Call,
                    keyboardType = KeyboardType.Phone,
                    isError = phoneError,
                    scale = scale
                )

                AuthFieldCompact(
                    value = password,
                    onValueChange = { 
                        password = it
                        passwordError = it.isBlank()
                    },
                    placeholder = stringResource(R.string.enter_password),
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible },
                    isError = passwordError,
                    scale = scale
                )
            }

            if (isLoginMode) {
                Text(
                    text = stringResource(R.string.forgot_password_q),
                    color = Color(0xFF1B4332),
                    fontSize = (13 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = (12 * scale).dp)
                        .clickable { onForgotPassword() }
                )
            }

            Spacer(modifier = Modifier.height((32 * scale).dp))

            // --- MAIN ACTION BUTTON ---
            Button(
                onClick = { 
                    nameError = !isLoginMode && name.isBlank()
                    phoneError = phone.isBlank()
                    passwordError = password.isBlank()

                    if (phoneError || passwordError || (!isLoginMode && nameError)) {
                        Toast.makeText(context, context.getString(R.string.error_fill_fields), Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (isLoginMode) {
                        authViewModel.login(phone, password)
                    } else {
                        authViewModel.register(name, phone, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height((56 * scale).dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                enabled = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isLoginMode) stringResource(R.string.login) else stringResource(R.string.register),
                            fontSize = (18 * scale).sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size((20 * scale).dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height((24 * scale).dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0).copy(alpha = 0.5f))
                Text(
                    text = stringResource(R.string.or_text),
                    color = Color(0xFF1B4332),
                    fontSize = (12 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0).copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height((24 * scale).dp))

            // --- GOOGLE SIGN IN ---
            OutlinedButton(
                onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier.fillMaxWidth().height((56 * scale).dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0).copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.continue_with_google), 
                        color = Color(0xFF1B4332), 
                        fontWeight = FontWeight.Bold,
                        fontSize = (15 * scale).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height((24 * scale).dp))

            // --- SECONDARY ACTION ---
            TextButton(
                onClick = { 
                    isLoginMode = !isLoginMode 
                    nameError = false
                    phoneError = false
                    passwordError = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isLoginMode) stringResource(R.string.create_new_account) else stringResource(R.string.back_to_login),
                    color = Color(0xFF1B4332),
                    fontSize = (15 * scale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height((32 * scale).dp))
        }
    }
}

@Composable
fun AuthToggleTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    scale: Float
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape((12 * scale).dp))
            .background(if (isSelected) Color(0xFF1B4332) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF1B4332),
            fontSize = (14 * scale).sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun AuthFieldCompact(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {},
    isError: Boolean = false,
    scale: Float
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, fontSize = (14 * scale).sp, color = Color(0xFF52796F).copy(alpha = 0.6f)) },
        leadingIcon = { Icon(icon, null, Modifier.size((20 * scale).dp), tint = if (isError) Color.Red else Color(0xFF1B4332)) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = if (isError) Color.Red else Color(0xFF1B4332),
                        modifier = Modifier.size((20 * scale).dp)
                    )
                }
            }
        } else if (isError) {
            { Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.size((20 * scale).dp)) }
        } else null,
        isError = isError,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
            focusedBorderColor = Color(0xFF1B4332),
            unfocusedBorderColor = Color(0xFFE0E0E0).copy(alpha = 0.5f),
            errorBorderColor = Color.Red,
            errorLeadingIconColor = Color.Red,
            errorTrailingIconColor = Color.Red
        ),
        singleLine = true,
        textStyle = TextStyle(fontSize = (15 * scale).sp, color = Color(0xFF1B4332), fontWeight = FontWeight.Medium)
    )
}

@Composable
fun LanguageSwitchItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    scale: Float
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF1B4332),
            fontSize = (12 * scale).sp,
            fontWeight = FontWeight.Bold
        )
    }
}
