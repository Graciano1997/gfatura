# 🧾 GFatura – Java Print Invoice Project

GFatura is a simple Java application that demonstrates how to parse JSON data representing an invoice (Fatura) and print its contents in a readable format. It uses the Jackson library to handle JSON serialization/deserialization.

📁 Project Structure

```bash

gfatura/
│
├── src/                 # Java source files (e.g., Main.java, Fatura.java)
├── lib/                 # External libraries (Jackson JARs)
│   ├── jackson-core-2.17.0.jar
│   ├── jackson-databind-2.17.0.jar
│   └── jackson-annotations-2.17.0.jar
├── bin/                 # Compiled .class files and jar (generated)
├── build.sh            # Shell script for Linux/macOS
├── build.bat           # Batch script for Windows
├── manifest.txt        # Manifest file for jar packaging
└── gfatura.jar         # Output executable jar (generated)
```

📦 Dependencies

This project requires the Jackson libraries:

    jackson-core-2.17.0.jar

    jackson-databind-2.17.0.jar

    jackson-annotations-2.17.0.jar

Make sure these files are present in the lib/ folder.
You can download them from: https://repo1.maven.org/maven2/com/fasterxml/jackson/

⚙️ Build & Run
✅ Option 1: Linux/macOS (with build.sh)

```bash
chmod +x build.sh
./build.sh
cd bin
java -jar ../gfatura.jar
```

✅ Option 2: Windows (with build.bat)

Open Command Prompt:

```bash

build.bat
cd bin
java -jar ..\gfatura.jar

```

📥 Example JSON Input

Inside the Main.java, the program uses a sample JSON like this:

```bash

{
  "numero": "FAT-001",
  "cliente": "João Silva",
  "data": "2025-07-18",
  "itens": [
    {"descricao": "Produto A", "quantidade": 2, "precoUnitario": 50.0},
    {"descricao": "Produto B", "quantidade": 1, "precoUnitario": 75.0}
  ]
}
```

🖨️ Output Example

```bash

Fatura: FAT-001
Cliente: Graciano Henrique
Data: 2025-07-18
--------------------------
Produto A | Qtde: 2 | Preço: 50.0 | Total: 100.0
Produto B | Qtde: 1 | Preço: 75.0 | Total: 75.0
--------------------------
Total Geral: 175.0
```

📜 License

This project is for educational purposes. Feel free to use and adapt it as needed.

✨ Author

Graciano Henrique
[LinkedIn](https://www.linkedin.com/in/gracianohenrique/) | [Github](https://github.com/Graciano1997/)
