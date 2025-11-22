# Commit Helper (JetBrains Plugin)

AI-powered **commit message generator** for JetBrains IDEs (IntelliJ IDEA, WebStorm, PyCharm, Rider, etc.).  
Generate clear, consistent commit messages with one click — following your team’s rules and conventions.

---

## ✨ Features
- One-click commit message generation in the Commit toolwindow  
- **Multi-Provider Support:** Gemini, Mistral, and **Ollama (Local)**
- **Multi-Language Support:** 11 languages including English, Turkish, Spanish, German, etc.
- **Smart Diff:** Accurately processes only selected files in the commit dialog
- Hybrid approach: custom template + AI completion  
- Enforces team conventions (prefix, length, type)  
- Configurable via **Settings → Tools → Commit Helper**  
- Offline fallback when AI is unavailable  

---

## 📦 Installation

### From Marketplace (recommended)
1. Open **Settings → Plugins → Marketplace**  
2. Search for **“Commit Helper”**  
3. Click **Install** and restart your IDE  

---

## 🚀 Usage
1. Open **Settings → Tools → Commit Helper** and configure:
   - AI provider (Gemini, Mistral, or Ollama for local models)
   - API key / endpoint  
   - Language (English, Turkish, Spanish, German, French, Italian, Portuguese, Russian, Chinese, Japanese, Korean)
   - Max length and template  
2. In the Commit window, select the files you want to commit.
3. Click **Commit Helper**  
4. Review, edit, and commit as usual  

---

## ⚙️ Configuration

**Template example (Conventional Commits):**
<type>(<scope>): <subject>

**Rules:**
- Subject ≤ 72 characters  
- Types: feat, fix, docs, refactor, perf, test, chore  
- Language selectable (11 options)  

---

## 🧩 Supported IDEs
- IntelliJ IDEA, WebStorm, PyCharm, Rider, and other JetBrains IDEs  
- Supported versions defined in `plugin.xml`  

---

## 🤝 Contributing
Contributions are welcome!  

1. Fork → create branch (`feat/xyz`)  
2. Implement + test  
3. Open a PR with description/screenshots  

---

## 🔑 API Keys & Privacy

Commit Helper does **not** provide or bundle any AI models.  
Instead, it connects to external APIs (**Gemini**, **Mistral**) using your **own API key**.

- You must obtain an API key from [Google AI Studio (Gemini)](https://aistudio.google.com/app/apikey) or [Mistral](https://console.mistral.ai/api-keys).
- Keys are stored **locally and securely** via IntelliJ’s built-in [PasswordSafe](https://www.jetbrains.com/help/idea/passwords.html).
- Your keys are **never sent anywhere else** — they are only used directly in API calls to the chosen provider.
- The plugin does **not** collect telemetry, analytics, or usage data.

⚠️ Usage of Gemini or Mistral is subject to their respective Terms of Service.

---

## 📄 License
This project is licensed under the MIT License © Mete Ülken.

Includes an icon from the *Industrial Sharp UI Icons* collection (MIT, Siemens).

---

## 📌 Links
- JetBrains Marketplace (coming soon)  
- [Source code](https://github.com/meteulken/intellij-commit-helper)  


