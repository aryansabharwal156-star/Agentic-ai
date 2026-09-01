package com.example.data.local

object SeedData {
    fun getInitialBusinesses(): List<BusinessEntity> {
        val rawData = listOf(
            Triple(1L, "Bella Italia Trattoria", "Restaurant") to listOf("UK / London", "+44 20 7946 0958", "info@bellaitalia.example", "www.bellaitalia.example", "No online reservation system; high call volume during peak hours.", 5, "new", 4200.0),
            Triple(2L, "Smile Dental Studio", "Dental Clinic") to listOf("USA / New York", "+1 212-555-0134", "contact@smiledental.example", "www.smiledental.example", "Misses calls after hours; no AI chatbot for appointment booking.", 5, "demo_booked", 8500.0),
            Triple(3L, "Luxe Hair & Spa", "Salon") to listOf("Canada / Toronto", "+1 416-555-0147", "hello@luxehair.example", "www.luxehair.example", "Manual booking only; website outdated, no online scheduling.", 4, "new", 3400.0),
            Triple(4L, "Prime Realty Group", "Real Estate") to listOf("Australia / Sydney", "+61 2 5550 1234", "admin@primerealty.example", "www.primerealty.example", "High lead volume from property listings; needs 24/7 lead capture.", 5, "new", 15000.0),
            Triple(5L, "Johnson & Associates Law", "Law Firm") to listOf("USA / Chicago", "+1 312-555-0182", "office@johnsonlaw.example", "www.johnsonlaw.example", "No live chat; potential clients leave voicemail after hours.", 4, "new", 12000.0),
            Triple(6L, "Arctic Air HVAC", "HVAC Services") to listOf("Canada / Vancouver", "+1 604-555-0111", "service@arcticair.example", "www.arcticair.example", "Emergency calls missed; no website booking for maintenance.", 5, "new", 9200.0),
            Triple(7L, "QuickFix Auto Repair", "Auto Repair") to listOf("UK / Manchester", "+44 161 555 0132", "bookings@quickfix.example", "www.quickfix.example", "No online appointment system; phone lines busy all day.", 5, "new", 5800.0),
            Triple(8L, "Zen Yoga Studio", "Fitness") to listOf("USA / Los Angeles", "+1 213-555-0128", "info@zenyoga.example", "www.zenyoga.example", "Class bookings only by phone; many inquiries about schedules.", 4, "new", 3100.0),
            Triple(9L, "Wanderlust Travel Co.", "Travel Agency") to listOf("Ireland / Dublin", "+353 1 555 0127", "trips@wanderlust.example", "www.wanderlust.example", "High inquiry rate; no AI to answer FAQs about packages.", 4, "new", 7500.0),
            Triple(10L, "Sparkle Clean Services", "Cleaning Service") to listOf("Australia / Melbourne", "+61 3 5550 0142", "hello@sparkleclean.example", "www.sparkleclean.example", "Misses calls while staff are on jobs; no quote request form.", 5, "new", 4900.0),
            Triple(11L, "GreenLeaf Landscaping", "Home Services") to listOf("USA / Austin", "+1 512-555-0164", "office@greenleaf.example", "www.greenleaf.example", "Seasonal demand; needs AI to capture leads 24/7.", 4, "new", 6200.0),
            Triple(12L, "Dr. Patel's Clinic", "Medical Clinic") to listOf("UK / Birmingham", "+44 121 555 0141", "reception@drpatel.example", "www.drpatel.example", "High call volume for appointments; no online booking.", 5, "new", 7800.0),
            Triple(13L, "Urban Cuts Barbershop", "Salon") to listOf("Canada / Montreal", "+1 514-555-0119", "book@urbancuts.example", "www.urbancuts.example", "Walk-ins only; no website for booking or pricing.", 4, "new", 2900.0),
            Triple(14L, "Bayview Property Mgmt", "Property Management") to listOf("USA / Miami", "+1 305-555-0177", "info@bayviewpm.example", "www.bayviewpm.example", "Tenant maintenance requests via phone only; no portal.", 5, "new", 11000.0),
            Triple(15L, "Peak Performance Gym", "Fitness") to listOf("Australia / Brisbane", "+61 7 5550 0155", "admin@peakgym.example", "www.peakgym.example", "No online membership sign-up; calls missed during classes.", 4, "new", 4100.0),
            Triple(16L, "The Taco Truck", "Restaurant") to listOf("USA / San Diego", "+1 619-555-0124", "orders@tacotruck.example", "www.tacotruck.example", "Food truck – needs AI to take orders when busy.", 5, "new", 5200.0),
            Triple(17L, "EverBright Electrical", "Electrician") to listOf("UK / Leeds", "+44 113 555 0138", "hello@everbright.example", "www.everbright.example", "Emergency calls after hours; no quote form on website.", 5, "new", 6800.0),
            Triple(18L, "Coastal Realty", "Real Estate") to listOf("USA / Charleston", "+1 843-555-0169", "info@coastalrealty.example", "www.coastalrealty.example", "High tourist inquiries; needs AI to qualify leads instantly.", 5, "new", 18000.0),
            Triple(19L, "Serenity Spa & Wellness", "Spa") to listOf("Canada / Calgary", "+1 403-555-0113", "relax@serenityspa.example", "www.serenityspa.example", "No online booking; staff busy with clients, miss calls.", 4, "new", 3900.0),
            Triple(20L, "Legal Aid Network", "Law Firm") to listOf("Australia / Perth", "+61 8 5550 0178", "contact@legalaidnet.example", "www.legalaidnet.example", "High volume of initial consultations; needs AI triage.", 5, "new", 8900.0),
            Triple(21L, "Brew & Bean Café", "Restaurant") to listOf("USA / Portland", "+1 503-555-0121", "hello@brewandbean.example", "www.brewandbean.example", "No online ordering; phone orders often missed during rush.", 4, "new", 2800.0),
            Triple(22L, "Pristine Pools", "Pool Service") to listOf("USA / Phoenix", "+1 602-555-0149", "service@pristinepools.example", "www.pristinepools.example", "Seasonal demand; no online scheduling for cleanings.", 4, "new", 5100.0),
            Triple(23L, "Maple Dental Care", "Dental Clinic") to listOf("Canada / Ottawa", "+1 613-555-0106", "smiles@mapledental.example", "www.mapledental.example", "No automated reminders; high no-show rate.", 3, "new", 4700.0),
            Triple(24L, "Silverline Plumbing", "Plumber") to listOf("UK / Glasgow", "+44 141 555 0129", "jobs@silverline.example", "www.silverline.example", "Emergency calls after hours; no website quote form.", 5, "new", 7300.0),
            Triple(25L, "FitLife 24/7 Gym", "Fitness") to listOf("Australia / Adelaide", "+61 8 5550 0193", "join@fitlife.example", "www.fitlife.example", "No AI for membership inquiries; high call volume.", 4, "new", 3800.0),
            Triple(26L, "Casa Blanca Mexican", "Restaurant") to listOf("USA / Houston", "+1 713-555-0158", "info@casablanca.example", "www.casablanca.example", "No reservation system; phone lines busy weekends.", 5, "new", 5900.0),
            Triple(27L, "TechFix Computer Repair", "IT Services") to listOf("UK / Edinburgh", "+44 131 555 0166", "support@techfix.example", "www.techfix.example", "Misses calls while on repairs; no online booking.", 4, "new", 4300.0),
            Triple(28L, "Aesthetic Dental Group", "Dental Clinic") to listOf("USA / Seattle", "+1 206-555-0136", "smile@aestheticdent.example", "www.aestheticdent.example", "No AI chatbot for FAQs; high number of new patient calls.", 4, "new", 9600.0),
            Triple(29L, "Harbourview Hotels", "Hospitality") to listOf("Canada / Halifax", "+1 902-555-0117", "stay@harbourview.example", "www.harbourview.example", "No AI to answer booking questions 24/7.", 5, "new", 13500.0),
            Triple(30L, "Elite Auto Detailing", "Auto Service") to listOf("Australia / Gold Coast", "+61 7 5550 0163", "book@elitedetail.example", "www.elitedetail.example", "No online booking; calls missed while detailing cars.", 4, "new", 3700.0),
            Triple(31L, "Sunny Day Preschool", "Education") to listOf("USA / Denver", "+1 303-555-0112", "hello@sunnyday.example", "www.sunnyday.example", "High parent inquiries; no AI to answer enrollment FAQs.", 4, "new", 6400.0),
            Triple(32L, "Gordon & Co. Solicitors", "Law Firm") to listOf("UK / Liverpool", "+44 151 555 0146", "law@gordonco.example", "www.gordonco.example", "No live chat; clients leave voicemails after hours.", 3, "new", 6100.0),
            Triple(33L, "Blue Ocean Realty", "Real Estate") to listOf("USA / Honolulu", "+1 808-555-0173", "aloha@blueocean.example", "www.blueocean.example", "Vacation rental inquiries 24/7; no AI to respond instantly.", 5, "new", 16500.0),
            Triple(34L, "The Nail Lounge", "Salon") to listOf("Canada / Edmonton", "+1 780-555-0191", "nails@naillounge.example", "www.naillounge.example", "No online booking; phone lines busy on weekends.", 4, "new", 3200.0),
            Triple(35L, "Alpine Heating & Air", "HVAC") to listOf("USA / Denver", "+1 303-555-0184", "fix@alpinehvac.example", "www.alpinehvac.example", "Emergency calls missed; no website scheduling.", 5, "new", 8700.0),
            Triple(36L, "City Bistro", "Restaurant") to listOf("Ireland / Cork", "+353 21 555 0134", "eat@citybistro.example", "www.citybistro.example", "No reservation system; high call volume evenings.", 5, "new", 4600.0),
            Triple(37L, "Dr. Smith Chiropractic", "Medical Clinic") to listOf("Canada / Winnipeg", "+1 204-555-0115", "care@drsmith.example", "www.drsmith.example", "No online appointment booking; high no-show rate.", 4, "new", 5300.0),
            Triple(38L, "Oceanview Property Mgmt", "Property Management") to listOf("Australia / Gold Coast", "+61 7 5550 0148", "info@oceanviewpm.example", "www.oceanviewpm.example", "Tenant requests via phone only; no online portal.", 5, "new", 10500.0),
            Triple(39L, "Fresh Cuts Lawn Care", "Home Services") to listOf("USA / Atlanta", "+1 404-555-0167", "green@freshcuts.example", "www.freshcuts.example", "Seasonal leads; no website quote form.", 4, "new", 4400.0),
            Triple(40L, "Velocity Fitness", "Gym") to listOf("UK / Newcastle", "+44 191 555 0150", "train@velocity.example", "www.velocity.example", "No AI for membership inquiries; calls missed during classes.", 4, "new", 3600.0),
            Triple(41L, "Premier Auto Sales", "Auto Dealership") to listOf("USA / Detroit", "+1 313-555-0123", "sales@premierauto.example", "www.premierauto.example", "High lead volume; no AI to qualify buyers instantly.", 5, "new", 22000.0),
            Triple(42L, "Lakeside Dental", "Dental Clinic") to listOf("Canada / Saskatoon", "+1 306-555-0196", "office@lakesidedent.example", "www.lakesidedent.example", "No online booking; front desk overwhelmed.", 4, "new", 6700.0),
            Triple(43L, "Espresso Lane Coffee", "Café") to listOf("Australia / Perth", "+61 8 5550 0129", "coffee@espressolane.example", "www.espressolane.example", "No online ordering; calls missed during rush.", 4, "new", 2600.0),
            Triple(44L, "Secure Home Alarms", "Security Services") to listOf("UK / Bristol", "+44 117 555 0168", "protect@securehome.example", "www.securehome.example", "High inquiry rate; no AI to answer FAQ about packages.", 4, "new", 5700.0),
            Triple(45L, "Tropical Travel Co.", "Travel Agency") to listOf("USA / Miami", "+1 305-555-0116", "go@tropicaltravel.example", "www.tropicaltravel.example", "No AI to answer 24/7 travel questions.", 3, "new", 6900.0),
            Triple(46L, "Happy Paws Pet Grooming", "Pet Services") to listOf("Canada / Victoria", "+1 250-555-0172", "woof@happypaws.example", "www.happypaws.example", "No online booking; calls missed during grooming.", 4, "new", 3100.0),
            Triple(47L, "Main Street Dental", "Dental Clinic") to listOf("Ireland / Galway", "+353 91 555 0118", "smiles@mainstreetdental.example", "www.mainstreetdental.example", "No online scheduling; high call volume.", 5, "new", 7200.0),
            Triple(48L, "Bright Spark Electric", "Electrician") to listOf("USA / Boston", "+1 617-555-0143", "service@brightspark.example", "www.brightspark.example", "Emergency calls after hours; no quote form.", 5, "new", 8100.0),
            Triple(49L, "Urban Nest Realty", "Real Estate") to listOf("UK / Manchester", "+44 161 555 0175", "homes@urbannest.example", "www.urbannest.example", "High renter inquiries; needs AI to pre-qualify leads.", 5, "new", 11500.0),
            Triple(50L, "Fit & Fuel Meal Prep", "Food Service") to listOf("Australia / Sydney", "+61 2 5550 0146", "eat@fitandfuel.example", "www.fitandfuel.example", "No online ordering; calls missed during meal prep.", 4, "new", 4900.0)
        )

        return rawData.map { (info, details) ->
            val (id, name, industry) = info
            val location = details[0] as String
            val phone = details[1] as String
            val email = details[2] as String
            val website = details[3] as String
            val whyNeed = details[4] as String
            val rating = details[5] as Int
            val status = details[6] as String
            val missedRev = details[7] as Double

            val (services, pricing, personaName) = getIndustryDetails(industry, name)
            val prompt = generateDefaultPrompt(name, industry, location, services, pricing, personaName)

            BusinessEntity(
                id = id,
                name = name,
                industry = industry,
                countryCity = location,
                phone = phone,
                email = email,
                website = website,
                whyNeedVoiceBot = whyNeed,
                rating = rating,
                status = status,
                estMonthlyMissedRev = missedRev,
                accountNotes = "High potential account. Target for AI Voice Demo pitch.",
                customHours = "Mon-Fri: 8:00 AM - 7:00 PM, Sat: 9:00 AM - 5:00 PM",
                servicesList = services,
                pricingGuide = pricing,
                botPersonaName = personaName,
                botVoicePitch = 1.0f,
                botVoiceSpeed = 1.0f,
                systemPrompt = prompt,
                demoCode = "DEMO-%04d".format(id * 137 % 10000),
                emailSentCount = if (status == "demo_booked") 1 else 0,
                lastContactedDate = if (status == "demo_booked") System.currentTimeMillis() - 86400000L * 2 else 0L,
                createdAt = System.currentTimeMillis() - (50 - id) * 3600000L
            )
        }
    }

    private fun getIndustryDetails(industry: String, businessName: String): Triple<String, String, String> {
        return when {
            industry.contains("Dental", true) -> Triple(
                "Emergency Extractions, Routine Teeth Cleaning, Deep Scaling, Teeth Whitening, Root Canals, Invisalign Consultation",
                "Cleaning: $95, Whitening: $299, Emergency Exam: $120, Full Consultation: Free",
                "Sarah (Dental AI Care Coordinator)"
            )
            industry.contains("Restaurant", true) || industry.contains("Café", true) || industry.contains("Food", true) -> Triple(
                "Table Reservations, Group Dining Events, Takeout Orders, Dietary & Allergen Inquiries, Private Parties",
                "Average Entrée: $18-$38, Chef Tasting Menu: $75/person, Group Booking Deposit: $50",
                "Marco (AI Maître d' & Host)"
            )
            industry.contains("Salon", true) || industry.contains("Spa", true) || industry.contains("Grooming", true) -> Triple(
                "Haircuts & Styling, Balayage & Color, Deep Tissue Massage, Facial Rejuvenation, Manicure & Pedicure",
                "Cut & Style: $65, Color & Highlights: $140, 60-min Massage: $95, Express Mani: $40",
                "Chloe (Spa & Salon Concierge)"
            )
            industry.contains("Real Estate", true) || industry.contains("Property", true) -> Triple(
                "Property Viewings, Buyer Consultations, Home Valuations, Rental Applications, Tenant Maintenance Triage",
                "Free Home Valuation, Buyer Agent Services, Property Mgmt: 7% monthly rent",
                "David (Senior Real Estate Voice AI)"
            )
            industry.contains("Law", true) -> Triple(
                "Personal Injury Case Evaluation, Family Law Consultation, Business Contract Review, Estate Planning",
                "Initial 20-min Strategy Call: Free, Retainer Hourly Rate: $320",
                "Elena (Legal Intake AI Specialist)"
            )
            industry.contains("HVAC", true) || industry.contains("Plumb", true) || industry.contains("Electric", true) || industry.contains("Home", true) -> Triple(
                "24/7 Emergency Repairs, AC System Tune-Up, Furnace Replacement, Leak Detection, Electrical Panel Upgrades",
                "Diagnostic Service Call: $89 (Waived with repair), Standard Tune-Up: $129",
                "Mike (Emergency Dispatch AI)"
            )
            industry.contains("Auto", true) -> Triple(
                "Brake Inspection & Repair, Full Synthetic Oil Change, Engine Diagnostic, Wheel Alignment, Ceramic Coating",
                "Oil Change: $69, Brake Pad Replacement: $189, Full Detail Package: $199",
                "Jake (Service Lane Voice AI)"
            )
            industry.contains("Fitness", true) || industry.contains("Gym", true) -> Triple(
                "1-on-1 Personal Training, Unlimited Class Passes, Free 3-Day Guest Pass, Body Composition Analysis",
                "Monthly Membership: $49/mo, 10-Class Pack: $140, Personal Training: $60/session",
                "Coach Maya (Fitness AI Advisor)"
            )
            else -> Triple(
                "General Consultations, Schedule Appointments, Service Quotes, Customer Support",
                "Standard Consultation: Free, Custom Quote upon inquiry",
                "Alex (AI Customer Concierge)"
            )
        }
    }

    private fun generateDefaultPrompt(
        businessName: String,
        industry: String,
        location: String,
        services: String,
        pricing: String,
        personaName: String
    ): String {
        return """
            You are $personaName, an exceptionally professional, warm, and proactive AI voice agent for '$businessName' in $location.
            Industry: $industry.
            Key Services: $services.
            Pricing & Rates: $pricing.
            
            YOUR VOICE OBJECTIVES:
            1. Greet callers cheerfully and identify yourself as the voice assistant for $businessName.
            2. Answer questions concisely with natural speech (2-3 sentences max per response) suitable for spoken phone calls.
            3. Actively help callers book appointments, request emergency services, get quotes, or check hours.
            4. Capture customer information (Name, Phone number, Desired service/time) politely.
            5. Never give long robotic lists; speak warmly, clearly, and conversationally.
        """.trimIndent()
    }

    fun getInitialSampleCallLogs(): List<CallLogEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            CallLogEntity(
                id = 1L,
                businessId = 2L, // Smile Dental Studio
                callerName = "Jessica Taylor",
                callerPhone = "+1 (212) 555-8912",
                callDate = now - 1800000L, // 30 mins ago
                durationSeconds = 84,
                status = "completed",
                inquiryType = "Emergency Booking",
                sentiment = "Positive",
                summary = "Caller had a severe toothache and requested same-day emergency appointment. AI collected contact info and scheduled slot for 4:30 PM.",
                actionItems = "Confirmed appointment with Dr. Chen at 4:30 PM today; sent SMS directions.",
                capturedLeadName = "Jessica Taylor",
                capturedLeadPhone = "+1 (212) 555-8912",
                capturedLeadEmail = "jessica.t@example.com",
                capturedLeadNotes = "Severe molar ache, upper right quadrant.",
                leadValueEstimate = 350.0,
                fullTranscriptJson = """[
                    {"speaker":"AI Voice Bot","text":"Thank you for calling Smile Dental Studio! I'm Sarah, the AI assistant. How can I help you today?"},
                    {"speaker":"Customer","text":"Hi Sarah, I have a terrible throbbing toothache on my upper right side. Do you have any emergency appointments available today?"},
                    {"speaker":"AI Voice Bot","text":"I'm so sorry you're in pain Jessica. We have an emergency slot open at 4:30 PM today with Dr. Chen. Would you like me to reserve that for you?"},
                    {"speaker":"Customer","text":"Yes please, that would be wonderful!"},
                    {"speaker":"AI Voice Bot","text":"You're all set for 4:30 PM today at our New York clinic. I've sent a confirmation text to this number. Please arrive 10 minutes early."}
                ]"""
            ),
            CallLogEntity(
                id = 2L,
                businessId = 1L, // Bella Italia Trattoria
                callerName = "Liam Davies",
                callerPhone = "+44 20 7123 4567",
                callDate = now - 7200000L, // 2 hrs ago
                durationSeconds = 52,
                status = "completed",
                inquiryType = "Table Reservation",
                sentiment = "Positive",
                summary = "Caller booked an anniversary dinner table for 4 guests on Friday at 7:30 PM with outdoor patio seating.",
                actionItems = "Patio table #12 reserved for Friday 7:30 PM; flagged anniversary celebration note.",
                capturedLeadName = "Liam Davies",
                capturedLeadPhone = "+44 20 7123 4567",
                capturedLeadEmail = "liam.d@example.co.uk",
                capturedLeadNotes = "Anniversary dinner, 4 guests, patio requested.",
                leadValueEstimate = 220.0,
                fullTranscriptJson = """[
                    {"speaker":"AI Voice Bot","text":"Ciao! Welcome to Bella Italia Trattoria. This is Marco. How may I assist your dining plans today?"},
                    {"speaker":"Customer","text":"Hello, I'd like to book a table for 4 this coming Friday at 7:30 PM for our anniversary."},
                    {"speaker":"AI Voice Bot","text":"Happy Anniversary! We would love to host you. We have a lovely table on our heated patio at 7:30 PM. Shall I book that under your name Liam?"},
                    {"speaker":"Customer","text":"Yes, perfect. Thank you Marco!"},
                    {"speaker":"AI Voice Bot","text":"Table reserved! We look forward to celebrating with you this Friday at Bella Italia."}
                ]"""
            ),
            CallLogEntity(
                id = 3L,
                businessId = 6L, // Arctic Air HVAC
                callerName = "Robert Chen",
                callerPhone = "+1 (604) 555-7890",
                callDate = now - 14400000L, // 4 hrs ago
                durationSeconds = 95,
                status = "completed",
                inquiryType = "Emergency Repair",
                sentiment = "Positive",
                summary = "Caller reported heat pump failure in sub-zero weather. AI dispatched on-call emergency technician and verified service fee.",
                actionItems = "Technician Dave dispatched for 1:00 PM arrival; $89 diagnostic fee acknowledged.",
                capturedLeadName = "Robert Chen",
                capturedLeadPhone = "+1 (604) 555-7890",
                capturedLeadEmail = "robert.c@example.ca",
                capturedLeadNotes = "Heat pump blown fuse / no heat.",
                leadValueEstimate = 480.0,
                fullTranscriptJson = """[
                    {"speaker":"AI Voice Bot","text":"Arctic Air 24/7 Emergency Dispatch, this is Mike. What heating or cooling issue can we resolve for you?"},
                    {"speaker":"Customer","text":"Our heat pump stopped working this morning and the house is freezing! Can someone come out today?"},
                    {"speaker":"AI Voice Bot","text":"We can definitely help Robert. We have an on-call technician Dave who can be at your Vancouver address between 1:00 PM and 2:00 PM today. Our diagnostic fee is $89 which gets applied toward any repair."},
                    {"speaker":"Customer","text":"Yes please send him over as soon as possible."},
                    {"speaker":"AI Voice Bot","text":"Technician Dave is dispatched to you. You'll receive a live tracking link via SMS shortly. Stay warm!"}
                ]"""
            )
        )
    }
}
