package com.example.cardioguard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val BASE_URL = "https://cardioguard-backend-9g9x.onrender.com/"
private const val TAG = "CardioGuard"

// Indian States and Districts
val INDIAN_STATES = listOf(
    "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
    "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
    "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
    "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
    "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
    "Uttar Pradesh", "Uttarakhand", "West Bengal"
)

val STATE_DISTRICTS = mapOf(
    "Tamil Nadu" to listOf(
        "Ariyalur", "Chengalpattu", "Chennai", "Coimbatore", "Cuddalore",
        "Dharmapuri", "Dindigul", "Erode", "Kallakurichi", "Kanchipuram",
        "Kanyakumari", "Karur", "Krishnagiri", "Madurai", "Mayiladuthurai",
        "Nagapattinam", "Namakkal", "Nilgiris", "Perambalur", "Pudukkottai",
        "Ramanathapuram", "Ranipet", "Salem", "Sivaganga", "Tenkasi",
        "Thanjavur", "Theni", "Thoothukudi", "Tiruchirappalli", "Tirunelveli",
        "Tirupathur", "Tiruppur", "Tiruvallur", "Tiruvannamalai", "Tiruvarur",
        "Vellore", "Viluppuram", "Virudhunagar"
    ),
    "Maharashtra" to listOf(
        "Ahmednagar", "Akola", "Amravati", "Aurangabad", "Beed",
        "Bhandara", "Buldhana", "Chandrapur", "Dhule", "Gadchiroli",
        "Gondia", "Hingoli", "Jalgaon", "Jalna", "Kolhapur",
        "Latur", "Mumbai City", "Mumbai Suburban", "Nagpur", "Nanded",
        "Nandurbar", "Nashik", "Osmanabad", "Palghar", "Parbhani",
        "Pune", "Raigad", "Ratnagiri", "Sangli", "Satara",
        "Sindhudurg", "Solapur", "Thane", "Wardha", "Washim", "Yavatmal"
    ),
    "Karnataka" to listOf(
        "Bagalkot", "Ballari", "Belagavi", "Bengaluru Rural", "Bengaluru Urban",
        "Bidar", "Chamarajanagar", "Chikballapur", "Chikkamagaluru", "Chitradurga",
        "Dakshina Kannada", "Davanagere", "Dharwad", "Gadag", "Hassan",
        "Haveri", "Kalaburagi", "Kodagu", "Kolar", "Koppal",
        "Mandya", "Mysuru", "Raichur", "Ramanagara", "Shivamogga",
        "Tumakuru", "Udupi", "Uttara Kannada", "Vijayapura", "Yadgir"
    ),
    "Kerala" to listOf(
        "Alappuzha", "Ernakulam", "Idukki", "Kannur", "Kasaragod",
        "Kollam", "Kottayam", "Kozhikode", "Malappuram", "Palakkad",
        "Pathanamthitta", "Thiruvananthapuram", "Thrissur", "Wayanad"
    ),
    "Andhra Pradesh" to listOf(
        "Anantapur", "Chittoor", "East Godavari", "Guntur", "Krishna",
        "Kurnool", "Nellore", "Prakasam", "Srikakulam", "Visakhapatnam",
        "Vizianagaram", "West Godavari", "YSR Kadapa"
    ),
    "Telangana" to listOf(
        "Adilabad", "Bhadradri Kothagudem", "Hyderabad", "Jagtial", "Jangaon",
        "Jayashankar Bhupalpally", "Jogulamba Gadwal", "Kamareddy", "Karimnagar", "Khammam",
        "Komaram Bheem", "Mahabubabad", "Mahbubnagar", "Mancherial", "Medak",
        "Medchal–Malkajgiri", "Nagarkurnool", "Nalgonda", "Nirmal", "Nizamabad",
        "Peddapalli", "Rajanna Sircilla", "Ranga Reddy", "Sangareddy", "Siddipet",
        "Suryapet", "Vikarabad", "Wanaparthy", "Warangal Rural", "Warangal Urban",
        "Yadadri Bhuvanagiri"
    ),
    "Gujarat" to listOf(
        "Ahmedabad", "Amreli", "Anand", "Aravalli", "Banaskantha",
        "Bharuch", "Bhavnagar", "Botad", "Chhota Udaipur", "Dahod",
        "Dang", "Devbhoomi Dwarka", "Gandhinagar", "Gir Somnath", "Jamnagar",
        "Junagadh", "Kheda", "Kutch", "Mahisagar", "Mehsana",
        "Morbi", "Narmada", "Navsari", "Panchmahal", "Patan",
        "Porbandar", "Rajkot", "Sabarkantha", "Surat", "Surendranagar",
        "Tapi", "Vadodara", "Valsad"
    ),
    "Rajasthan" to listOf(
        "Ajmer", "Alwar", "Banswara", "Baran", "Barmer",
        "Bharatpur", "Bhilwara", "Bikaner", "Bundi", "Chittorgarh",
        "Churu", "Dausa", "Dholpur", "Dungarpur", "Ganganagar",
        "Hanumangarh", "Jaipur", "Jaisalmer", "Jalore", "Jhalawar",
        "Jhunjhunu", "Jodhpur", "Karauli", "Kota", "Nagaur",
        "Pali", "Pratapgarh", "Rajsamand", "Sawai Madhopur", "Sikar",
        "Sirohi", "Tonk", "Udaipur"
    ),
    "Uttar Pradesh" to listOf(
        "Agra", "Aligarh", "Allahabad", "Ambedkar Nagar", "Amethi",
        "Amroha", "Auraiya", "Azamgarh", "Baghpat", "Bahraich",
        "Ballia", "Balrampur", "Banda", "Barabanki", "Bareilly",
        "Basti", "Bhadohi", "Bijnor", "Budaun", "Bulandshahr",
        "Chandauli", "Chitrakoot", "Deoria", "Etah", "Etawah",
        "Faizabad", "Farrukhabad", "Fatehpur", "Firozabad", "Gautam Buddha Nagar",
        "Ghaziabad", "Ghazipur", "Gonda", "Gorakhpur", "Hamirpur",
        "Hapur", "Hardoi", "Hathras", "Jalaun", "Jaunpur",
        "Jhansi", "Kannauj", "Kanpur Dehat", "Kanpur Nagar", "Kasganj",
        "Kaushambi", "Kushinagar", "Lakhimpur Kheri", "Lalitpur", "Lucknow",
        "Maharajganj", "Mahoba", "Mainpuri", "Mathura", "Mau",
        "Meerut", "Mirzapur", "Moradabad", "Muzaffarnagar", "Pilibhit",
        "Pratapgarh", "Raebareli", "Rampur", "Saharanpur", "Sambhal",
        "Sant Kabir Nagar", "Shahjahanpur", "Shamli", "Shravasti", "Siddharthnagar",
        "Sitapur", "Sonbhadra", "Sultanpur", "Unnao", "Varanasi"
    ),
    "West Bengal" to listOf(
        "Alipurduar", "Bankura", "Birbhum", "Cooch Behar", "Dakshin Dinajpur",
        "Darjeeling", "Hooghly", "Howrah", "Jalpaiguri", "Jhargram",
        "Kalimpong", "Kolkata", "Malda", "Murshidabad", "Nadia",
        "North 24 Parganas", "Paschim Bardhaman", "Paschim Medinipur", "Purba Bardhaman", "Purba Medinipur",
        "Purulia", "South 24 Parganas", "Uttar Dinajpur"
    )
    // Add more states as needed
)

// Global patient data
object PatientData {
    var name: String = ""
    var email: String = ""
    var district: String = ""
    var state: String = ""
    var gender: Int = -1  // Changed to -1 for no default selection
    var age: String = ""
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CardioGuardApp()
            }
        }
    }
}

@Composable
fun CardioGuardApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "terms") {
        composable("terms") {
            Log.d(TAG, "Navigation: Terms Screen")
            TermsAndConditionsScreen(navController)
        }
        composable("home") {
            Log.d(TAG, "Navigation: Patient Profile Screen")
            PatientProfileScreen(navController)
        }
        composable("predict") {
            Log.d(TAG, "Navigation: Cardiac Assessment Screen")
            CardiacAssessmentScreen(navController)
        }
        composable(
            route = "result/{risk}/{confidence}",
            arguments = listOf(
                navArgument("risk") { type = NavType.StringType },
                navArgument("confidence") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val risk = backStackEntry.arguments?.getString("risk") ?: "Unknown"
            val confidence = backStackEntry.arguments?.getString("confidence") ?: "0.0"
            Log.d(TAG, "Navigation: Result Screen - Risk: $risk, Confidence: $confidence")
            ResultScreen(navController, risk, confidence)
        }
    }
}

/* ---------------- TERMS AND CONDITIONS SCREEN (PROFESSIONAL) ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(navController: NavController) {
    var acceptedTerms by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E), // Deep medical blue
                        Color(0xFF283593),
                        Color(0xFF3949AB)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))

                // Professional Medical Icon with ECG wave
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Color.White,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // ECG Wave Symbol
                        Text(
                            "⎯⎯⎯∿∿⎯⎯⎯",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935), // Medical red
                            letterSpacing = (-2).sp
                        )
                        Spacer(Modifier.height(4.dp))
                        // Heart with pulse
                        Text(
                            "❤",
                            fontSize = 28.sp,
                            color = Color(0xFFE53935)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "CardioGuard",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Intelligent Alert System for",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Normal
                )

                Text(
                    text = "Cardiac Arrest Detection",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(32.dp))

            // Terms and Conditions Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        "Terms and Conditions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    Spacer(Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            "Please read and accept the following terms and conditions:",
                            fontSize = 14.sp,
                            color = Color(0xFF616161),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(16.dp))

                        TermItem(
                            number = "1",
                            title = "Medical Disclaimer",
                            content = "This application provides cardiac risk assessment based on entered medical data. It is NOT a substitute for professional medical advice, diagnosis, or treatment. Always consult with qualified healthcare providers for medical decisions."
                        )

                        TermItem(
                            number = "2",
                            title = "Data Privacy",
                            content = "Your personal and medical information will be processed securely and used solely for risk assessment purposes. We comply with applicable data protection regulations and maintain strict confidentiality."
                        )

                        TermItem(
                            number = "3",
                            title = "Accuracy & Limitations",
                            content = "While our assessment system uses validated algorithms, results are estimates and may not reflect all individual health factors. The accuracy depends on the correctness of entered data."
                        )

                        TermItem(
                            number = "4",
                            title = "Emergency Situations",
                            content = "In case of chest pain, shortness of breath, or other cardiac emergency symptoms, immediately call emergency services (108/112). Do NOT rely on this app for emergency medical care."
                        )

                        TermItem(
                            number = "5",
                            title = "User Responsibility",
                            content = "You are responsible for providing accurate medical information. Inaccurate data may lead to incorrect risk assessments. Keep your health records updated and share results with your healthcare provider."
                        )

                        TermItem(
                            number = "6",
                            title = "Professional Consultation",
                            content = "High-risk assessments require immediate medical consultation. Follow up with cardiologists or healthcare professionals for detailed evaluation and treatment planning."
                        )

                        Spacer(Modifier.height(8.dp))
                    }

                    Divider(color = Color(0xFFE0E0E0))

                    Spacer(Modifier.height(16.dp))

                    // Acceptance Checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = acceptedTerms,
                            onCheckedChange = { acceptedTerms = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF1A237E)
                            )
                        )
                        Text(
                            "I have read and agree to the Terms and Conditions",
                            fontSize = 14.sp,
                            color = Color(0xFF212121),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Continue Button
            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A237E),
                    disabledContainerColor = Color(0xFF1A237E).copy(alpha = 0.5f)
                ),
                enabled = acceptedTerms
            ) {
                Text(
                    "Continue to Registration",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun TermItem(number: String, title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row {
            Text(
                "$number.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF42A5F5),
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            content,
            fontSize = 13.sp,
            color = Color(0xFF616161),
            lineHeight = 18.sp,
            modifier = Modifier.padding(start = 20.dp)
        )
    }
}

/* ---------------- PATIENT PROFILE SCREEN WITH DROPDOWNS ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedState by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(-1) } // -1 = not selected

    var stateExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }

    val availableDistricts = if (selectedState.isNotEmpty()) {
        STATE_DISTRICTS[selectedState] ?: emptyList()
    } else {
        emptyList()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4DD0E1),
                        Color(0xFF64B5F6),
                        Color(0xFF42A5F5)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Patient Registration",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Please provide your details for medical assessment",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(24.dp))

            // Full Name
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Full Name *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Enter full name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Age
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Age (years) *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        placeholder = { Text("Enter age") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Gender - NOW REQUIRES SELECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Gender *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = gender == 1,
                                onClick = { gender = 1 }
                            )
                            Text("Male", modifier = Modifier.padding(start = 4.dp))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = gender == 0,
                                onClick = { gender = 0 }
                            )
                            Text("Female", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Email
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Email Address *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("example@email.com") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // State Dropdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "State *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = stateExpanded,
                        onExpandedChange = { stateExpanded = !stateExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedState,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Select state") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, "Select State")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = stateExpanded,
                            onDismissRequest = { stateExpanded = false }
                        ) {
                            INDIAN_STATES.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(state) },
                                    onClick = {
                                        selectedState = state
                                        selectedDistrict = "" // Reset district when state changes
                                        stateExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // District Dropdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "District *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = districtExpanded,
                        onExpandedChange = {
                            if (selectedState.isNotEmpty()) {
                                districtExpanded = !districtExpanded
                            }
                        }
                    ) {
                        OutlinedTextField(
                            value = selectedDistrict,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text(if (selectedState.isEmpty()) "Select state first" else "Select district") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, "Select District")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(8.dp),
                            enabled = selectedState.isNotEmpty()
                        )
                        ExposedDropdownMenu(
                            expanded = districtExpanded,
                            onDismissRequest = { districtExpanded = false }
                        ) {
                            availableDistricts.forEach { district ->
                                DropdownMenuItem(
                                    text = { Text(district) },
                                    onClick = {
                                        selectedDistrict = district
                                        districtExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = {
                    // Save patient data
                    PatientData.name = name
                    PatientData.email = email
                    PatientData.district = selectedDistrict
                    PatientData.state = selectedState
                    PatientData.gender = gender
                    PatientData.age = age
                    Log.d(TAG, "Patient saved: $name, Age: $age, Gender: $gender, State: $selectedState, District: $selectedDistrict")
                    navController.navigate("predict")
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF42A5F5)
                ),
                enabled = name.isNotBlank() && email.isNotBlank() &&
                        selectedDistrict.isNotBlank() && selectedState.isNotBlank() &&
                        age.isNotBlank() && gender != -1
            ) {
                Text("Continue to Assessment", fontSize = 16.sp)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ---------------- CARDIAC ASSESSMENT SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardiacAssessmentScreen(navController: NavController) {
    var hr by remember { mutableStateOf("") }
    var sbp by remember { mutableStateOf("") }
    var dbp by remember { mutableStateOf("") }
    var sugar by remember { mutableStateOf("") }
    var ckmb by remember { mutableStateOf("") }
    var troponin by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4DD0E1),
                        Color(0xFF64B5F6),
                        Color(0xFF42A5F5)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(bottom = 80.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Cardiac Risk Assessment",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Patient: ${PatientData.name}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )

            Text(
                "Please enter vital signs and biomarkers",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(24.dp))

            MedicalField("Heart Rate (bpm)", hr) { hr = it }
            Spacer(Modifier.height(16.dp))
            MedicalField("Systolic BP (mmHg)", sbp) { sbp = it }
            Spacer(Modifier.height(16.dp))
            MedicalField("Diastolic BP (mmHg)", dbp) { dbp = it }
            Spacer(Modifier.height(16.dp))
            MedicalField("Blood Sugar (mg/dL)", sugar) { sugar = it }
            Spacer(Modifier.height(16.dp))
            MedicalField("CK-MB (ng/mL)", ckmb) { ckmb = it }
            Spacer(Modifier.height(16.dp))
            MedicalField("Troponin (ng/mL)", troponin) { troponin = it }

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = {
                    Log.d(TAG, "Calculate Risk button clicked")
                    if (validateFields(PatientData.age, hr, sbp, dbp, sugar, ckmb, troponin)) {
                        Log.d(TAG, "Validation passed, calling backend...")
                        isLoading = true
                        errorMessage = ""
                        callBackend(
                            age = PatientData.age,
                            gender = PatientData.gender,
                            hr = hr,
                            sbp = sbp,
                            dbp = dbp,
                            sugar = sugar,
                            ckmb = ckmb,
                            troponin = troponin,
                            onResult = { risk, confidence ->
                                Log.d(TAG, "Got result: Risk=$risk, Confidence=$confidence")
                                isLoading = false
                                navController.navigate("result/$risk/$confidence")
                            },
                            onError = { error ->
                                Log.e(TAG, "Got error: $error")
                                isLoading = false
                                errorMessage = error
                            }
                        )
                    } else {
                        Log.w(TAG, "Validation failed")
                        errorMessage = "Please fill all fields with valid values"
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF42A5F5)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Analyzing...", fontSize = 16.sp)
                } else {
                    Text("Calculate Risk", fontSize = 16.sp)
                }
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = "Error",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF44336)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = errorMessage,
                                color = Color(0xFF212121),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalField(label: String, value: String, onValueChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Enter $label") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }
    }
}

fun validateFields(
    age: String, hr: String, sbp: String, dbp: String,
    sugar: String, ckmb: String, troponin: String
): Boolean {
    return try {
        val valid = age.toInt() in 1..120 &&
                hr.toInt() in 30..200 &&
                sbp.toInt() in 50..250 &&
                dbp.toInt() in 30..150 &&
                sugar.toInt() in 50..500 &&
                ckmb.toDouble() >= 0 &&
                troponin.toDouble() >= 0
        Log.d(TAG, "Validation result: $valid")
        valid
    } catch (e: Exception) {
        Log.e(TAG, "Validation error: ${e.message}")
        false
    }
}

/* ---------------- RESULT SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController, risk: String, confidence: String) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Decode URL-encoded parameters
    val decodedRisk = try {
        java.net.URLDecoder.decode(risk, StandardCharsets.UTF_8.toString())
    } catch (e: Exception) {
        Log.e(TAG, "Error decoding risk: ${e.message}")
        risk
    }

    val decodedConfidence = try {
        java.net.URLDecoder.decode(confidence, StandardCharsets.UTF_8.toString())
    } catch (e: Exception) {
        Log.e(TAG, "Error decoding confidence: ${e.message}")
        confidence
    }

    Log.d(TAG, "Result screen showing - Risk: $decodedRisk, Confidence: $decodedConfidence")

    val riskColor = when (decodedRisk.uppercase()) {
        "LOW RISK", "LOW" -> Color(0xFF4CAF50)
        "MODERATE RISK", "MEDIUM RISK", "MODERATE", "MEDIUM" -> Color(0xFFFF9800)
        "HIGH RISK", "HIGH" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    val recommendation = when (decodedRisk.uppercase()) {
        "LOW RISK", "LOW" -> "Your cardiac risk assessment indicates a low risk level. Continue maintaining a healthy lifestyle with regular exercise and a balanced diet."
        "MODERATE RISK", "MEDIUM RISK", "MODERATE", "MEDIUM" -> "Your cardiac risk assessment indicates a moderate risk level. We recommend consulting with a healthcare professional and making lifestyle modifications."
        "HIGH RISK", "HIGH" -> "⚠️ CRITICAL: Your cardiac risk assessment indicates a HIGH RISK level. Immediate medical attention is recommended. Emergency alert has been prepared."
        else -> "Unable to determine risk level. Please consult with a healthcare professional."
    }

    val isHighRisk = decodedRisk.uppercase().contains("HIGH")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4DD0E1),
                        Color(0xFF64B5F6),
                        Color(0xFF42A5F5)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Risk Assessment Result",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(riskColor.copy(alpha = 0.2f), RoundedCornerShape(40.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isHighRisk) "⚠️" else "💙",
                            fontSize = 40.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = decodedRisk.uppercase(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Confidence: ${decodedConfidence}%",
                        fontSize = 18.sp,
                        color = Color(0xFF212121)
                    )

                    Spacer(Modifier.height(16.dp))

                    Divider(color = Color(0xFFE0E0E0))

                    Spacer(Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Patient Information",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Name: ${PatientData.name}", fontSize = 14.sp)
                        Text("Age: ${PatientData.age} years", fontSize = 14.sp)
                        Text("Gender: ${if (PatientData.gender == 1) "Male" else "Female"}", fontSize = 14.sp)
                        Text("Location: ${PatientData.district}, ${PatientData.state}", fontSize = 14.sp)
                        Text("Email: ${PatientData.email}", fontSize = 14.sp)
                    }

                    Spacer(Modifier.height(16.dp))

                    Divider(color = Color(0xFFE0E0E0))

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = recommendation,
                        fontSize = 14.sp,
                        color = Color(0xFF212121),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (isHighRisk) {
                Button(
                    onClick = {
                        sendEmergencyAlert(context, decodedRisk, decodedConfidence)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("🚨 Send Emergency Alert", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    navController.navigate("predict") {
                        popUpTo("predict") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF42A5F5)
                )
            ) {
                Text("New Assessment", fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    navController.navigate("terms") {
                        popUpTo("terms") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Back to Home", fontSize = 16.sp)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ---------------- EMERGENCY ALERT FUNCTION ---------------- */

fun sendEmergencyAlert(context: android.content.Context, risk: String, confidence: String) {
    val subject = "🚨 URGENT: High Cardiac Risk Alert - ${PatientData.name}"

    val message = """
        ⚠️ CARDIAC RISK ALERT ⚠️
        
        PATIENT DETAILS:
        ━━━━━━━━━━━━━━━━━━━━━━
        Name: ${PatientData.name}
        Age: ${PatientData.age} years
        Gender: ${if (PatientData.gender == 1) "Male" else "Female"}
        Email: ${PatientData.email}
        Location: ${PatientData.district}, ${PatientData.state}
        
        ASSESSMENT RESULTS:
        ━━━━━━━━━━━━━━━━━━━━━━
        Risk Level: $risk
        Confidence: $confidence%
        
        ⚠️ IMMEDIATE ACTION REQUIRED ⚠️
        
        This patient has been assessed with HIGH cardiac risk. 
        Immediate medical evaluation and intervention is recommended.
        
        Please contact the patient urgently at:
        Email: ${PatientData.email}
        
        Generated by CardioGuard App
        Date: ${java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())}
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(PatientData.email, "emergency@hospital.com"))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, message)
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Send Emergency Alert via:"))
    } catch (e: Exception) {
        val smsIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(smsIntent, "Share Alert via:"))
    }
}

/* ---------------- API CALL (FIXED WITH PROPER ENCODING) ---------------- */

fun callBackend(
    age: String,
    gender: Int,
    hr: String,
    sbp: String,
    dbp: String,
    sugar: String,
    ckmb: String,
    troponin: String,
    onResult: (String, String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val json = JSONObject().apply {
            put("patient", JSONObject().apply {
                put("age", age.toInt())
                put("gender", gender)
            })
            put("medical", JSONObject().apply {
                put("heart_rate", hr.toInt())
                put("systolic_bp", sbp.toInt())
                put("diastolic_bp", dbp.toInt())
                put("blood_sugar", sugar.toInt())
                put("ck_mb", ckmb.toDouble())
                put("troponin", troponin.toDouble())
            })
        }

        Log.d(TAG, "=== API CALL START ===")
        Log.d(TAG, "URL: $BASE_URL/predict")
        Log.d(TAG, "Request JSON: $json")

        val request = Request.Builder()
            .url("$BASE_URL/predict")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "=== API CALL FAILED ===")
                Log.e(TAG, "Error: ${e.message}", e)
                Handler(Looper.getMainLooper()).post {
                    onError("Network error: ${e.message}. Backend may be waking up (wait 50 seconds).")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "=== API RESPONSE ===")
                    Log.d(TAG, "Response Code: ${response.code}")
                    Log.d(TAG, "Response Body: $responseBody")

                    if (response.isSuccessful && responseBody != null) {
                        val body = JSONObject(responseBody)
                        val risk = body.optString("risk", "Unknown")
                        val confidence = body.optDouble("confidence", 0.0)

                        Log.d(TAG, "Parsed - Risk: '$risk', Confidence: $confidence")

                        Handler(Looper.getMainLooper()).post {
                            // URL encode the risk to handle spaces
                            val encodedRisk = URLEncoder.encode(risk, StandardCharsets.UTF_8.toString())
                            val confidenceStr = String.format("%.1f", confidence)

                            Log.d(TAG, "Encoded Risk: '$encodedRisk', Confidence: '$confidenceStr'")
                            Log.d(TAG, "=== CALLING onResult ===")

                            onResult(encodedRisk, confidenceStr)
                        }
                    } else {
                        Log.e(TAG, "API Error - Code: ${response.code}, Body: $responseBody")
                        Handler(Looper.getMainLooper()).post {
                            onError("Server error (${response.code}): ${responseBody ?: "Unknown error"}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Response parsing error: ${e.message}", e)
                    Handler(Looper.getMainLooper()).post {
                        onError("Error parsing response: ${e.message}")
                    }
                }
            }
        })
    } catch (e: Exception) {
        Log.e(TAG, "Request creation error: ${e.message}", e)
        Handler(Looper.getMainLooper()).post {
            onError("Request error: ${e.message}")
        }
    }
}