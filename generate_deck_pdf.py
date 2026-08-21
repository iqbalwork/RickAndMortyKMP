import zlib
import os

class SimpleSlidePDF:
    def __init__(self, filename="RickAndMorty_KMP_Presentation_Draft.pdf", width=960, height=540):
        self.filename = filename
        self.width = width
        self.height = height
        self.pages = []
        self.objects = []
        
    def _add_object(self, content):
        self.objects.append(content)
        return len(self.objects)

    def draw_rounded_rect(self, x, y, w, h, r, fill_rgb=None, stroke_rgb=None, stroke_width=1):
        """Draw rounded or regular rectangle in PDF content stream"""
        stream = []
        if stroke_rgb:
            stream.append(f"{stroke_rgb[0]:.3f} {stroke_rgb[1]:.3f} {stroke_rgb[2]:.3f} RG")
            stream.append(f"{stroke_width} w")
        if fill_rgb:
            stream.append(f"{fill_rgb[0]:.3f} {fill_rgb[1]:.3f} {fill_rgb[2]:.3f} rg")
            
        # Approximation of rectangle
        stream.append(f"{x:.2f} {y:.2f} {w:.2f} {h:.2f} re")
        if fill_rgb and stroke_rgb:
            stream.append("B")
        elif fill_rgb:
            stream.append("f")
        elif stroke_rgb:
            stream.append("S")
        return "\n".join(stream)

    def escape_text(self, text):
        # Replace common unicode emojis with text representations
        replacements = {
            "🛸": "[Explorer] ",
            "🪐": "[Locations] ",
            "📺": "[Episodes] ",
            "💾": "[Offline] ",
            "🎨": "[Design] ",
            "🛡️": "[Adaptive] ",
            "⚡": "[Speed] ",
            "🚀": "[KMP] ",
            "📦": "[Repo] ",
            "💬": "[Community] ",
            "👨‍💻": "[Speaker] ",
            "🎙️": "[Host] ",
            "✅": "[YES] ",
            "⚠️": "[NOTE] ",
            "→": "->",
            "—": " - ",
            "–": "-",
            '"': '"',
            '"': '"',
            ''': "'",
            ''': "'",
        }
        for k, v in replacements.items():
            text = text.replace(k, v)
        # Encode unencodable chars safely
        clean_text = "".join([c if ord(c) < 256 else "?" for c in text])
        return clean_text.replace('\\', '\\\\').replace('(', '\\(').replace(')', '\\)')

    def add_slide(self, slide_data):
        """
        slide_data: dict with
          - slide_num: int
          - total_slides: int
          - category: str (e.g. "ARCHITECTURE", "OVERVIEW", "DEEP DIVE")
          - title: str
          - subtitle: str (optional)
          - columns: list of dicts: [ {"title": "...", "items": [...], "width_pct": 50, "bg_color": ...} ]
          - notes: str (speaker notes)
        """
        content_cmds = []
        w = self.width
        h = self.height

        # 1. Background (Dark Cyberpunk Theme: #0B0F19 -> rgb: 0.043, 0.059, 0.098)
        content_cmds.append(self.draw_rounded_rect(0, 0, w, h, 0, fill_rgb=(0.043, 0.059, 0.098)))
        
        # Subtle top accent bar (Portal Green: #00E676 -> rgb: 0.0, 0.902, 0.463)
        content_cmds.append(self.draw_rounded_rect(0, h - 5, w, 5, 0, fill_rgb=(0.0, 0.902, 0.463)))
        
        # Subtle glow element / accent box on header
        content_cmds.append(self.draw_rounded_rect(40, h - 35, 140, 18, 0, fill_rgb=(0.08, 0.15, 0.22), stroke_rgb=(0.0, 0.9, 0.46), stroke_width=0.8))

        # Header Badge & Slide Number
        cat_text = self.escape_text(slide_data.get("category", "KMP SHARING SESSION #01"))
        page_str = f"{slide_data.get('slide_num', 1)} / {slide_data.get('total_slides', 9)}"
        
        content_cmds.append(f"""
BT
/F2 8.5 Tf
0.0 0.902 0.463 rg
48 {h - 28} Td
({cat_text}) Tj
ET
BT
/F1 9 Tf
0.5 0.55 0.65 rg
{w - 75} {h - 28} Td
({page_str}) Tj
ET
""")

        # Slide Title
        title = self.escape_text(slide_data.get("title", ""))
        content_cmds.append(f"""
BT
/F2 22 Tf
1.0 1.0 1.0 rg
40 {h - 68} Td
({title}) Tj
ET
""")

        # Subtitle / description if present
        subtitle = slide_data.get("subtitle", "")
        y_content_start = h - 90
        if subtitle:
            content_cmds.append(f"""
BT
/F1 11 Tf
0.0 0.90 0.95 rg
40 {h - 90} Td
({self.escape_text(subtitle)}) Tj
ET
""")
            y_content_start = h - 110

        # Draw Columns / Content Boxes
        columns = slide_data.get("columns", [])
        num_cols = len(columns)
        
        # Available content area
        notes_height = 80 if slide_data.get("notes") else 0
        content_height = (y_content_start - 35 - notes_height)
        
        left_margin = 40
        right_margin = 40
        col_gap = 20
        total_width = w - left_margin - right_margin
        
        if num_cols > 0:
            col_w = (total_width - (num_cols - 1) * col_gap) / num_cols
            
            for i, col in enumerate(columns):
                cx = left_margin + i * (col_w + col_gap)
                cy = 35 + notes_height
                ch = content_height
                
                # Card Background (#131C2E -> 0.075, 0.11, 0.18)
                bg_rgb = col.get("bg_rgb", (0.075, 0.11, 0.18))
                border_rgb = col.get("border_rgb", (0.15, 0.22, 0.35))
                content_cmds.append(self.draw_rounded_rect(cx, cy, col_w, ch, 0, fill_rgb=bg_rgb, stroke_rgb=border_rgb, stroke_width=1))
                
                # Card Header
                col_title = col.get("title", "")
                if col_title:
                    content_cmds.append(self.draw_rounded_rect(cx, cy + ch - 30, col_w, 30, 0, fill_rgb=(0.10, 0.15, 0.25)))
                    content_cmds.append(f"""
BT
/F2 11 Tf
0.0 0.902 0.463 rg
{cx + 14} {cy + ch - 20} Td
({self.escape_text(col_title)}) Tj
ET
""")
                
                # Card Items
                item_y = cy + ch - 48
                items = col.get("items", [])
                for item in items:
                    if isinstance(item, tuple) or isinstance(item, list):
                        header, detail = item[0], item[1]
                        # Bullet dot
                        content_cmds.append(self.draw_rounded_rect(cx + 14, item_y + 3, 4, 4, 0, fill_rgb=(0.0, 0.90, 0.46)))
                        content_cmds.append(f"""
BT
/F2 9.5 Tf
0.95 0.95 0.98 rg
{cx + 24} {item_y} Td
({self.escape_text(header)}) Tj
ET
""")
                        item_y -= 14
                        # Detail text wrapping
                        detail_lines = self._wrap_text(detail, max_chars=int(col_w / 5.2))
                        for dl in detail_lines:
                            content_cmds.append(f"""
BT
/F1 8.5 Tf
0.65 0.72 0.82 rg
{cx + 24} {item_y} Td
({self.escape_text(dl)}) Tj
ET
""")
                            item_y -= 12
                        item_y -= 6
                    else:
                        # Simple bullet item
                        content_cmds.append(self.draw_rounded_rect(cx + 14, item_y + 3, 4, 4, 0, fill_rgb=(0.0, 0.90, 0.95)))
                        lines = self._wrap_text(str(item), max_chars=int(col_w / 5.2))
                        for idx, line in enumerate(lines):
                            content_cmds.append(f"""
BT
/F1 9 Tf
0.85 0.88 0.95 rg
{cx + 24} {item_y} Td
({self.escape_text(line)}) Tj
ET
""")
                            item_y -= 13
                        item_y -= 4

        # Draw Speaker Notes Box at bottom
        notes = slide_data.get("notes")
        if notes:
            ny = 25
            nw = w - 80
            nh = notes_height - 10
            content_cmds.append(self.draw_rounded_rect(40, ny, nw, nh, 0, fill_rgb=(0.05, 0.08, 0.13), stroke_rgb=(0.2, 0.3, 0.45), stroke_width=0.6))
            content_cmds.append(f"""
BT
/F2 8 Tf
0.95 0.75 0.1 rg
52 {ny + nh - 14} Td
(SPEAKER SCRIPT / NOTES:) Tj
ET
""")
            note_lines = self._wrap_text(notes, max_chars=130)
            note_y = ny + nh - 28
            for nl in note_lines[:3]:
                content_cmds.append(f"""
BT
/F1 8 Tf
0.75 0.82 0.90 rg
52 {note_y} Td
({self.escape_text(nl)}) Tj
ET
""")
                note_y -= 11

        # Footer branding
        content_cmds.append(f"""
BT
/F1 7.5 Tf
0.4 0.45 0.55 rg
40 12 Td
(Rick & Morty KMP - UZIRO Sharing Session #01 | Speaker: Iqbal Fauzi) Tj
ET
""")

        raw_content = "\n".join(content_cmds).encode("latin-1")
        compressed_content = zlib.compress(raw_content)
        self.pages.append(compressed_content)

    def _wrap_text(self, text, max_chars=50):
        words = text.split(" ")
        lines = []
        cur = []
        cur_len = 0
        for w in words:
            if cur_len + len(w) + 1 > max_chars:
                if cur:
                    lines.append(" ".join(cur))
                cur = [w]
                cur_len = len(w)
            else:
                cur.append(w)
                cur_len += len(w) + (1 if cur_len > 0 else 0)
        if cur:
            lines.append(" ".join(cur))
        return lines

    def build(self):
        output = bytearray()
        output.extend(b"%PDF-1.4\n")
        
        # Object 1: Catalog
        # Object 2: Outlines
        # Object 3: Pages
        # Object 4: Font F1 (Helvetica)
        # Object 5: Font F2 (Helvetica-Bold)
        # For each page:
        #   Object Page
        #   Object Contents
        
        offsets = {}
        
        def write_obj(num, data):
            offsets[num] = len(output)
            output.extend(f"{num} 0 obj\n".encode("latin-1"))
            output.extend(data)
            output.extend(b"\nendobj\n")

        num_pages = len(self.pages)
        page_obj_ids = []
        content_obj_ids = []
        
        cur_id = 6
        for _ in range(num_pages):
            page_obj_ids.append(cur_id)
            content_obj_ids.append(cur_id + 1)
            cur_id += 2

        # 1: Catalog
        write_obj(1, b"<< /Type /Catalog /Pages 3 0 R >>")
        # 2: Outlines
        write_obj(2, b"<< /Type /Outlines /Count 0 >>")
        # 3: Pages
        kids_str = " ".join([f"{pid} 0 R" for pid in page_obj_ids])
        write_obj(3, f"<< /Type /Pages /Count {num_pages} /Kids [ {kids_str} ] >>".encode("latin-1"))
        # 4: Font F1
        write_obj(4, b"<< /Type /Font /Subtype /Type1 /Name /F1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>")
        # 5: Font F2
        write_obj(5, b"<< /Type /Font /Subtype /Type1 /Name /F2 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>")

        # Pages & Contents
        for i in range(num_pages):
            pid = page_obj_ids[i]
            cid = content_obj_ids[i]
            p_data = f"<< /Type /Page /Parent 3 0 R /MediaBox [ 0 0 {self.width} {self.height} ] /Contents {cid} 0 R /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> >>".encode("latin-1")
            write_obj(pid, p_data)
            
            c_stream = self.pages[i]
            c_data = f"<< /Length {len(c_stream)} /Filter /FlateDecode >>\nstream\n".encode("latin-1") + c_stream + b"\nendstream"
            write_obj(cid, c_data)

        # Xref
        xref_offset = len(output)
        total_objs = cur_id
        output.extend(f"xref\n0 {total_objs}\n".encode("latin-1"))
        output.extend(b"0000000000 65535 f \n")
        for i in range(1, total_objs):
            off = offsets.get(i, 0)
            output.extend(f"{off:010d} 00000 n \n".encode("latin-1"))

        # Trailer
        output.extend(f"trailer\n<< /Size {total_objs} /Root 1 0 R >>\nstartxref\n{xref_offset}\n%%EOF\n".encode("latin-1"))
        
        with open(self.filename, "wb") as f:
            f.write(output)
        print(f"Generated PDF: {self.filename} ({len(output)} bytes)")

if __name__ == "__main__":
    pdf = SimpleSlidePDF("/home/iqbalf/StudioProjects/RickAndMortyKMP/RickAndMorty_KMP_Presentation_Draft.pdf")
    
    slides = [
        {
            "slide_num": 1,
            "total_slides": 9,
            "category": "UZIRO KMP SHARING SESSION #01",
            "title": "From Zero to Your First Kotlin Multiplatform App",
            "subtitle": "Building a Production-Grade Cross-Platform Mobile App with Compose Multiplatform & Clean Architecture",
            "columns": [
                {
                    "title": "PRESENTER PROFILE",
                    "items": [
                        ("Speaker", "Iqbal Fauzi (Senior Mobile Engineer at Bobobox)"),
                        ("Moderator", "Bintang Poetra (Mobile Engineer at MaxxiTani)"),
                        ("Community", "UZIRO KMP Developer Community (bit.ly/uziro-kmp)"),
                        ("Topic Focus", "Kotlin 2.x, Compose Multiplatform 1.10, Room KMP, Ktor 3.3, Koin 4.1")
                    ]
                },
                {
                    "title": "WHAT YOU WILL LEARN",
                    "items": [
                        ("Modern Code-Sharing", "Understanding the code-sharing spectrum from logic to full UI."),
                        ("Clean Architecture & MVI", "Structuring scalable multiplatform codebase with single source of truth."),
                        ("Offline-First Strategy", "Leveraging Room KMP and Ktor for resilient data synchronization."),
                        ("Compose Multiplatform UI", "Crafting responsive, native-performance UI for Android & iOS.")
                    ]
                }
            ],
            "notes": "Halo teman-teman! Selamat datang di sharing session UZIRO #01. Hari ini kita bakal kupas tuntas bagaimana membangun aplikasi mobile Android & iOS sekaligus menggunakan 100% Kotlin Multiplatform dan Compose Multiplatform lewat sample project Rick and Morty KMP."
        },
        {
            "slide_num": 2,
            "total_slides": 9,
            "category": "FOUNDATIONS & MINDSET",
            "title": "Why Kotlin Multiplatform (KMP)?",
            "subtitle": "A pragmatic approach to code sharing without runtime compromises",
            "columns": [
                {
                    "title": "FLEXIBLE CODE-SHARING SPECTRUM",
                    "items": [
                        ("Logic-Only Sharing", "Share Business Logic, Data, and Network layer while writing 100% native UI in Swift/SwiftUI and Jetpack Compose."),
                        ("Full-Stack Multiplatform", "Share 100% of the UI & Logic using Compose Multiplatform across Android & iOS."),
                        ("Gradual Adoption", "Can be adopted module by module into existing production apps without rewriting everything.")
                    ]
                },
                {
                    "title": "KMP vs OTHER FRAMEWORKS",
                    "items": [
                        ("True Native Compilation", "Compiles directly to JVM bytecode on Android and native Apple Framework (LLVM/Objective-C interop) on iOS."),
                        ("Zero Runtime Overhead", "No heavy JavaScript bridge, webview container, or custom rendering engine overhead."),
                        ("Single Tech Stack", "Your mobile team writes Kotlin, uses standard coroutines, Flows, and familiar Android-like paradigms.")
                    ]
                }
            ],
            "notes": "KMP berbeda dengan hybrid engine lainnya karena tidak memaksakan runtime bridge. Kita bisa memilih porsi sharing sesuai kebutuhan: mau logic-nya aja, atau bablas sampai UI dengan Compose Multiplatform seperti di app Rick & Morty ini."
        },
        {
            "slide_num": 3,
            "total_slides": 9,
            "category": "PROJECT SHOWCASE",
            "title": "What We Built: Rick & Morty Multiverse Explorer",
            "subtitle": "Production-grade features designed to demonstrate real-world patterns",
            "columns": [
                {
                    "title": "CORE APP FEATURES",
                    "items": [
                        ("🛸 Multiverse Character Explorer", "Search, dynamic filtering by status/gender/species, and infinite pagination."),
                        ("🪐 Dimension & Location Guide", "Explore dimensions and characters residing in each location."),
                        ("📺 Interdimensional Episode Guide", "List of episodes categorized by seasons with character associations."),
                        ("💾 Favorites Vault (Offline-First)", "Bookmark characters and episodes stored locally in cross-platform Room SQLite DB.")
                    ]
                },
                {
                    "title": "UX & DESIGN HIGHLIGHTS",
                    "items": [
                        ("🎨 Cyberpunk Sci-Fi Design", "Material 3 tokens with PortalGreen, CyberYellow, and ElectricCyan glows."),
                        ("🛡️ Edge-to-Edge Adaptive Layout", "Clean window insets handling status bars and dynamic islands on iOS."),
                        ("⚡ High-Performance Image Loading", "Integrated Coil 3 with Ktor engine for smooth avatar and asset caching.")
                    ]
                }
            ],
            "notes": "App ini bukan sekadar 'Hello World', tapi dirancang mendekati standar production: handle offline caching, complex filtering, type-safe navigation, hingga custom Material 3 theming yang adaptif."
        },
        {
            "slide_num": 4,
            "total_slides": 9,
            "category": "SYSTEM DESIGN",
            "title": "Architecture Blueprint: Clean Architecture + MVI",
            "subtitle": "Unidirectional Data Flow (UDF) for predictable state management",
            "columns": [
                {
                    "title": "ARCHITECTURAL LAYERS",
                    "items": [
                        ("Presentation Layer", "Compose Multiplatform UI observes immutable StateFlow from MVI ViewModel."),
                        ("Domain Layer", "Reusable UseCases encapsulating business rules and data transformations."),
                        ("Data Layer", "Repository pattern serving as Single Source of Truth orchestrating Local & Remote."),
                        ("Data Sources", "Remote API via Ktor Client & Local Cache via Room KMP Database.")
                    ]
                },
                {
                    "title": "MVI DATA FLOW CYCLE",
                    "items": [
                        ("1. User Intent / Action", "User interacts with UI (e.g. OnSearchQuery, OnToggleFavorite)."),
                        ("2. ViewModel Execution", "ViewModel receives intent and triggers domain UseCase coroutine flow."),
                        ("3. Repository Resolution", "Fetches from Room cache or Ktor HTTP endpoint."),
                        ("4. State Emission", "ViewModel updates immutable UI state, re-rendering Compose UI smoothly.")
                    ]
                }
            ],
            "notes": "Kita mengadopsi Clean Architecture + MVI. ViewModel mengekspos single immutable UI State ke Compose, sementara Use Cases mengisolasi business logic sehingga logic layer benar-benar agnostic dari platform."
        },
        {
            "slide_num": 5,
            "total_slides": 9,
            "category": "TECH STACK",
            "title": "The Powerhouse Multiplatform Libraries",
            "subtitle": "Modern, stable multiplatform ecosystem powering Rick & Morty KMP",
            "columns": [
                {
                    "title": "CORE LIBRARIES (PART 1)",
                    "items": [
                        ("Compose Multiplatform 1.10.x", "100% shared declarative UI for Android & iOS."),
                        ("Navigation Compose KMP", "Official Jetpack Navigation with Kotlin Serializable type-safe routes."),
                        ("Koin Multiplatform 4.1.x", "Lightweight Dependency Injection with shared koinViewModel()."),
                        ("Kotlin Coroutines & Flow", "Asynchronous pipelines and reactive UI state management.")
                    ]
                },
                {
                    "title": "CORE LIBRARIES (PART 2)",
                    "items": [
                        ("Ktor Client 3.3.x", "Multiplatform HTTP engine (OkHttp on Android, Darwin on iOS)."),
                        ("Room Multiplatform 2.7.x", "Official Google SQLite ORM for KMP with KSP code generation."),
                        ("Kotlinx Serialization", "Blazing fast JSON serialization for API & Navigation args."),
                        ("Coil 3 Multiplatform", "Modern async image loading with memory and disk caching.")
                    ]
                }
            ],
            "notes": "Dulu KMP minim library official, tapi sekarang ekosistemnya sudah sangat matang: Room sudah resmi multiplatform dari Google, Navigation Compose sudah multiplatform, Coil 3 dan Koin juga full support."
        },
        {
            "slide_num": 6,
            "total_slides": 9,
            "category": "DEEP DIVE",
            "title": "Under the Hood: Offline-First Data Layer",
            "subtitle": "Writing shared SQLite database and networking code once in commonMain",
            "columns": [
                {
                    "title": "ROOM MULTIPLATFORM STRATEGY",
                    "items": [
                        ("Shared Schema & DAOs", "Entities and DAOs written once in commonMain with @Database annotations."),
                        ("Platform Driver Injection", "DatabaseBuilder created via Android context on Android and NSFileManager on iOS."),
                        ("Continuous Offline Sync", "Room DAOs expose Flow<List<Entity>> for automatic UI updates upon cache invalidation.")
                    ]
                },
                {
                    "title": "KTOR CLIENT & NETWORK RESILIENCE",
                    "items": [
                        ("Standardized HTTP Setup", "Common Ktor HttpClient with ContentNegotiation, logging (Napier), and timeout config."),
                        ("Network Result Wrapper", "Custom Result<T> sealed interface catching network anomalies cleanly."),
                        ("Repository Integration", "Cache-then-network pattern: return local data instantly, sync latest from API in background.")
                    ]
                }
            ],
            "notes": "Salah satu highlight menarik adalah Room KMP. Kita bisa menulis Room Database dan DAO persis seperti di native Android, tapi sekarang berjalan langsung di iOS tanpa perlu bridging manual SQLDelight."
        },
        {
            "slide_num": 7,
            "total_slides": 9,
            "category": "UI & NAVIGATION",
            "title": "Compose Multiplatform & Type-Safe Navigation",
            "subtitle": "Eliminating boilerplate and runtime navigation bugs across platforms",
            "columns": [
                {
                    "title": "TYPE-SAFE NAVIGATION GRAPH",
                    "items": [
                        ("@Serializable Route Objects", "Routes defined as Kotlin data classes instead of fragile string URL paths."),
                        ("Type-Safe Argument Passing", "Arguments (e.g. characterId) are automatically serialized and deserialized with full compile-time safety."),
                        ("Bottom Navigation Sync", "Centralized top-level destinations matching active route state.")
                    ]
                },
                {
                    "title": "ADAPTIVE DESIGN & THEME SYSTEM",
                    "items": [
                        ("Custom Design Tokens", "Material 3 cyberpunk theme with sci-fi portal color palettes."),
                        ("Window Insets & Edge-to-Edge", "WindowInsets.safeDrawing guarantees content never clashes with status bars or camera cutouts."),
                        ("Shared Component Library", "Custom CharacterCard, FilterChips, and StatusBadge reused across all tabs.")
                    ]
                }
            ],
            "notes": "Dengan Jetpack Navigation Compose KMP, navigasi antar screen sekarang type-safe menggunakan serializable objects. Passing arguments jadi sangat clean dan aman dari runtime crash."
        },
        {
            "slide_num": 8,
            "total_slides": 9,
            "category": "BEST PRACTICES",
            "title": "Practical Tips & Lessons Learned",
            "subtitle": "How to avoid common pitfalls when starting your KMP journey",
            "columns": [
                {
                    "title": "RECOMMENDED DO'S",
                    "items": [
                        ("Adopt Incrementally", "Start by sharing Data/Domain layer before moving UI to Compose Multiplatform."),
                        ("Use Gradle Version Catalogs", "Centralize dependencies in libs.versions.toml for seamless version alignment."),
                        ("Prefer DI over expect/actual", "Use interfaces and Koin dependency injection rather than excessive expect/actual declarations."),
                        ("Leverage Kotlin 2.0+ & K2", "Take advantage of faster compilation and improved Multiplatform tooling.")
                    ]
                },
                {
                    "title": "CRITICAL DON'TS",
                    "items": [
                        ("Don't Ignore iOS Physical Testing", "Always test scroll physics, font rendering, and gestures on real iOS devices or simulator."),
                        ("Don't Leak Platform APIs", "Keep domain Use Cases free from Android Context or iOS platform dependencies."),
                        ("Don't Block Main Thread", "Always dispatch database and network operations on Dispatchers.IO.")
                    ]
                }
            ],
            "notes": "Tips terbaik buat yang baru mulai: jangan over-engineer expect/actual. Cukup gunakan DI dan interface. Manfaatkan Version Catalog agar manajemen dependency tetap rapi."
        },
        {
            "slide_num": 9,
            "total_slides": 9,
            "category": "WRAP-UP & COMMUNITY",
            "title": "Wrap-Up: Connect, Clone & Build!",
            "subtitle": "Open-source resources to accelerate your Multiplatform mastery",
            "columns": [
                {
                    "title": "REPOSITORIES & RESOURCES",
                    "items": [
                        ("📦 GitHub Repository", "github.com/iqbalf/RickAndMortyKMP (Full source code, MIT license)"),
                        ("💬 UZIRO KMP Community", "Join Telegram & Discord community at bit.ly/uziro-kmp"),
                        ("👨‍💻 Speaker Profile", "github.com/iqbalf (Senior Mobile Engineer @ Bobobox)"),
                        ("🎙️ Moderator Profile", "github.com/bintangpoetra (Mobile Engineer @ MaxxiTani)")
                    ]
                },
                {
                    "title": "NEXT STEPS FOR YOU",
                    "items": [
                        ("1. Clone the project", "Run ./gradlew :androidApp:installDebug or open in Xcode."),
                        ("2. Explore the commonMain module", "Inspect how Room, Ktor, and Koin are configured in one place."),
                        ("3. Try adding a new feature", "Add episode search or location bookmarking using the existing pattern!"),
                        ("4. Q & A Session", "Feel free to ask any questions about KMP setup, architecture, or lessons learned!")
                    ]
                }
            ],
            "notes": "Repo-nya sudah open-source dan siap di-clone untuk bahan belajar atau starter template teman-teman. Sekarang kita buka sesi tanya jawab!"
        }
    ]
    
    for s in slides:
        pdf.add_slide(s)
        
    pdf.build()
