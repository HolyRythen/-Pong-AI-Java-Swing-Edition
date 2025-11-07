# 🕹️ Pong AI – Java Swing Edition

Ein klassisches **Pong-Spiel mit Computergegner (AI)** in purem **Java Swing**.  
Eine Datei, kein Framework, läuft überall mit Java 17+.  
Der Gegner reagiert dynamisch, macht kleine Fehler (je nach Distanz) und simuliert echte Reaktionszeiten. 🎯

---

## 🚀 Features

- 🧠 **Computergegner mit KI-Verhalten**
  - Reaktionsverzögerung (Frames)
  - Zielabweichung abhängig von Ballentfernung
  - Sanftes Nachführen der Bewegung
- ⚙️ Einstellbare **Schwierigkeit** (einfach, mittel, schwer)
- ⏸️ **Pause** und **Reset**
- 🧩 Komplett **offline & plattformunabhängig**
- 🖥️ **60 FPS flüssige Bewegung**
- 🎨 Minimalistisches, modernes UI-Design (Dark Mode)
- 🕹️ Steuerung über Tastatur oder Maus-Klick zum Start

---

## 🧩 Voraussetzungen

- Java 17 oder neuer  
- Keine externen Bibliotheken notwendig

---

## ⚙️ Installation & Start

### 🔧 Kompilieren
```powershell
javac PongAISwing.java
```

### ▶️ Starten
```powershell
java PongAISwing
```

> 💡 Funktioniert auf Windows, Linux und macOS gleichermaßen.  
> Nutzt Swing, also keine JavaFX-Module nötig.

---

## 🎮 Steuerung

| Taste | Funktion |
|--------|-----------|
| **W** / **↑** | Spieler nach oben |
| **S** / **↓** | Spieler nach unten |
| **P** / **ESC** | Pause |
| **R** | Reset (Neustart) |
| **H** | Hilfe‑Overlay ein/aus |

---

## 🧠 KI-Parameter

Im Code anpassbar (am Anfang der Klasse):

| Variable | Bedeutung | Beispielwert |
|-----------|------------|--------------|
| `AI_MAX_SPEED` | Max. Paddle‑Speed | `6.0` bis `9.0` |
| `AI_REACTION_FRAMES` | Frames zwischen Entscheidungen | `6` |
| `AI_SMOOTHING` | Sanftheit der Bewegung (0–1) | `0.18` |

Je höher die Werte, desto stärker oder reaktionsfreudiger spielt der Computer.

---

## 💡 Tipps

- Der Ball wird nach jedem Punkt mit leicht zufälligem Winkel gestartet.  
- Die KI macht kleine Zufallsfehler, wenn der Ball weit entfernt ist – das macht sie menschlicher.  
- Perfekt geeignet, um **Kollisionserkennung**, **Vektormathematik** und **2D‑Animation** in Java zu lernen.  

---

## 📸 Beispiel

```
┌──────────────────────────────────────────────┐
│                                              │
│     🏓 Spieler (links)        Gegner (rechts)│
│                                              │
│     Score: 3                Score: 4         │
│                                              │
│             [Ball fliegt nach rechts]        │
│                                              │
└──────────────────────────────────────────────┘
```

---

## 📄 Lizenz

MIT License — frei nutzbar, modifizierbar, kommerziell erlaubt.

---

© 2025 Robert Martin
