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
    "empresa": "Hosana",
    "nif": "005717487M045",
    "local": "Luanda, Cacuaco",
    "email": "hosana@gmail.com",
    "empresaPhone": "+244 935636086",
    "numeroRecibo": "021/2025",
    "dataEmissao": "19-07-2025 19:35:29",
    "vendedor": "Maria Prata",
    "troco": "779.28 kz",
    "telefone": 911111111,
    "cliente": "Henrique",
    "desconto": "0.0 kz",
    "total": "1220.72 kz",
    "formaPagamento": "CASH",
    "observacoes": "Lembre-se de seguir as orientações de uso dos medicamentos <br/>Em caso de dúvida, consulte nossa equipe..",
    "produto": [
      {
        "nome": "Cesartem",
        "qtd": 1,
        "preco": "200.0 kz"
      },
      {
        "nome": "Davimeter",
        "qtd": 1,
        "preco": "1000.0 kz"
      }
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
This project is for educational purposes. Feel free to use and adapt it as needed.......

✨ Author

Graciano Henrique
[LinkedIn](https://www.linkedin.com/in/gracianohenrique/) | [Github](https://github.com/Graciano1997/)
