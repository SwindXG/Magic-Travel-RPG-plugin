# Minecraft Plugin

這是一個基本的 Minecraft 插件專案，旨在展示如何創建一個簡單的插件。

## 目錄結構

```
minecraft-plugin
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── Main.java
│   │   └── resources
│   │       └── plugin.yml
├── build.gradle
└── README.md
```

## 安裝

1. 確保你已經安裝了 [Gradle](https://gradle.org/install/)。
2. 將專案克隆到你的本地機器。
3. 在專案根目錄下運行 `gradle build` 來構建插件。

## 使用

1. 將生成的 `.jar` 文件放入你的 Minecraft 伺服器的 `plugins` 目錄中。
2. 啟動伺服器，插件將自動加載。
3. 你可以在伺服器控制台中查看插件的啟用和禁用信息。

## 貢獻

歡迎任何形式的貢獻！如果你有建議或發現了錯誤，請提交問題或拉取請求。

## 授權

此專案使用 MIT 授權。詳情請參見 LICENSE 文件。