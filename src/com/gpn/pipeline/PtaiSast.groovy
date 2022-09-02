package com.gpn.pipeline
import static groovy.json.JsonOutput.*

/**
PtAI Parameres with description:

{
// Настройка основных параметров
"ProjectName": "Test_Proj", // Имя проекта
"ProgrammingLanguage": "Csharp", // Язык приложения: Java, Php, Csharp, VB, ObjectiveC, CPlusPlus, Sql, Swift, Python, JavaScript, Kotlin, Go
"ScanAppType": "CSharp , Configuration, Fingerprint, PmTaint ", // Модули поиска уязвимостей: Php, Java, CSharp, Configuration, Fingerprint, PmTaint , Blackbox, JavaScript
"ThreadCount": 1, // Количество потоков
"Site": "http://localhost", // Адрес сайта
"IsDownloadDependencies": true, // Загрузить зависимости
"IsUsePublicAnalysisMethod": false, // Искать от доступных public и protected методов
"IsUseEntryAnalysisPoint": true, // Искать от точек входа
"ScanUnitTimeout": 600, // Максимальное время сканирования файла в секундах
"PreprocessingTimeout": 60, // Тайм-аут препроцессинга в минутах
"CustomParameters": null, // Дополнительные параметры запуска
"SkipFileFormats": ["*.gif"], // Форматы файлов, исключенные из сканирования
"SkipFilesFolders": ["\\.git\\", "\\.gitignore", "\\.gitmodules", "\\.gitattributes", "\\$tf\\", "\\$BuildProcessTemplate\\", "\\.tfignore"], // Фильтр дерева
// Поиск уязвимостей
"DisabledPatterns": ["145", "146", "148", "149"], // Поиск по шаблонам, отключенные шаблоны
"DisabledTypes": [], // Проверки исходного кода, отключенные проверки исходного кода
"ConsiderPreviousScan": true, // Учитывать предыдущее сканирование
"UseIssueTrackerIntegration": true, // Использовать интеграцию с Jira
// Параметры языка Java
"IsUnpackUserPackages": false, // Распаковывать пользовательские пакеты
"JavaParameters": null, // Параметры запуска JDK
"JavaVersion": 0, // Версия JDK, 0 соответствует версии 1.8, 1 соответствует версии 1.11
// Параметры языка C#
"ProjectType": "Solution", // Типа проекта: Solution, WebSite
"SolutionFile": "path_to_solution.sln", // Путь к файлу решения/проекта Приложение А. Пример конфигурационного файла 54
"WebSiteFolder": "path_to_website", // Папка сайта
// Параметры языка JavaScript
"JavaScriptProjectFile": "path_to_file", // Путь к файлу скрипта
"JavaScriptProjectFolder": "path_to_dir", // Путь к корневой папке проекта javascript
// Параметры PMTaint
"UseTaintAnalysis": false, // Использовать taint-анализ
"UsePmAnalysis": true, // Использовать только pm-анализ
"DisableInterpretCores": false, // Игнорировать ядра интерпретации (C#, Java, PHP) при анализе
// Параметры базы знаний YARA
"UseDefaultFingerprints": true, // Использовать базу уязвимых компонентов PT AI
"UseCustomYaraRules": false, // Использовать пользовательские правила YARA
// Настройка параметров черного ящика
"BlackBoxScanLevel": "None", // Режим поиска: Fast, Normal, Full
"CustomHeaders": [["", ""]], // Дополнительные заголовки
"Authentication": {
"auth_item": {
"domain": null, // Адрес проверки
"credentials": {
"cookie": null, // Значение cookie
"type": 2, // Тип аутентификации: 0 = Form, 1 = HTTP, 2 = None, 3 = Cookie
"login": {
"name": null, // Ключ логина
"value": null, // Значения логина
"regexp": null,
"is_regexp": false
},
"password": {
"name": null, // Ключ пароля
"value": null, // Значения пароля
"regexp": null, // Например: "p[aA]ss(word)?"
"is_regexp": false
}
},
"test_url": null, // Адрес проверки
"form_url": null, // Адрес формы
"form_xpath": ".//form", // XPath-путь к форме
"regexp_of_success": null // Шаблон проверки
}
},
"ProxySettings": {
"IsEnabled": false, // Активировать параметры прокси-сервера Приложение А. Пример конфигурационного файла 55
"Host": null, // IP-адреc
"Port": null, // Порт
"Type": 0, // Тип прокси: 0 = HTTP, 1 = HTTPNOCONNECT, 2 = SOCKS4, 3 = SOCKS5
"Username": null, // Логин
"Password": null // Пароль
},
// Настройка автоматической проверки уязвимостей
"RunAutocheckAfterScan": false, // Запустить автопроверку после сканирования
"AutocheckSite": "http://localhost", // Адрес сайта для автопроверки, если адрес не указан, используется значение параметра "Site"
"AutocheckCustomHeaders": [["", ""]], // Дополнительные заголовки
"AutocheckAuthentication": {
"auth_item": {
"domain": null, // Адрес проверки
"credentials": {
"cookie": null, // Значение cookie
"cookies": null,
"type": 2, // Тип аутентификации: 0 = Form, 1 = HTTP, 2 = None, 3 = Cookie
"login": {
"name": null, // Ключ логина
"value": null, // Значения логина
"regexp": null,
"is_regexp": false
},
"password": {
"name": null, // Ключ пароля
"value": null, // Значения пароля
"regexp": null, // Например: "p[aA]ss(word)?"
"is_regexp": false
}
},
"test_url": null, // поле "Адрес проверки"
"form_url": null, // поле "Адрес формы"
"form_xpath": ".//form", // XPath-путь к форме
"regexp_of_success": null // Шаблон проверки
}
},
"AutocheckProxySettings": {
"IsEnabled": false, // Активировать параметры прокси-сервера
"Host": null, // IP-адрес
"Port": null, // Порт
"Type": 0, // Тип прокси: 0 = HTTP, 1 = HTTPNOCONNECT, 2 = SOCKS4, 3 = SOCKS5
"Username": null, // Логин
"Password": null // Пароль
}, 
"SendEmailWithReportsAfterScan": true, // Отправлять отчет на почту по завершении сканирования
"CompressReport": false, // Сжимать отчет перед отправкой
// Настройка отправки почты
"EmailSettings": {
"SmtpServerAddress": "mail.ptsecurity.ru", // Адрес SMTP-сервера
"UserName": "testagent_wes@ptsecurity.com", // Имя пользователя
"Password": "P@ssw0rd", // Пароль
"EmailRecipients": "User@ptsecurity.ru", // Адрес получателя отчета, вы можете указывать несколько адресов через ";"
"EnableSsl": true, // Включить SSL
"Subject": "Email Title", // Тема сообщения
"ConsiderCertificateError": true, // Учитывать ошибки сертификата
"SenderEmail": "testagent_wes@ptsecurity.com" // Отправитель
},
// Настройка отчета
"ReportParameters": {
"SaveAsPath": null, // Папка для сохранения отчетов
"UseFilters": false, // Использовать фильтры
"CreatePdfPrintVersion": false, // Создавать версию для печати
"IncludeDiscardedVulnerabilities": false, // Добавить в отчет опровергнутые уязвимости
"IncludeSuppressedVulnerabilities": false, // Добавить в отчет исключенные уязвимости
"IncludeSuspectedVulnerabilities": true, // Добавить в отчет подозрения на уязвимость
"IncludeGlossary": false, // Добавить в отчет справочник об уязвимостях
"IncludeDFD": false // Добавить диаграмму потока данных
}
}

*/

class PtaiSast extends SastFunction implements Saster {
    String sast_project_name
    String sast_filter_pattern
    String sast_hide_debug
    String sast_incremental
    String sast_report_email_list
    String sast_lang
    String sast_vulnerability_threshold_enabled
    Boolean is_debug


    String genSettingsJson(String repo_name, String branch_name) {
        // Defaults for settings
        Map config = [:]
        String scan_app_type = 'Configuration, Fingerprint, PmTaint'
        String custom_parameters = ''
        //String lang_specific_params = ''
        String site = 'http://localhost'

        // Define ScanAppType following by supported language set
        // Supported languages for PmTaint: [KOTLIN, JAVA, CPP, CSHARP, GO, JS, OBJECTIVEC, VB, SQL, SWIFT, PHP, PYTHON]
        switch(sast_lang.toLowerCase()) {
            case 'java':
                scan_app_type += ', Java'
                // Распаковывать пользовательские пакеты
                config['IsUnpackUserPackages'] = false
                // Параметры запуска JDK
                config['JavaParameters'] = null
                // Версия JDK, 0 соответствует версии 1.8, 1 соответствует версии 1.11
                config['JavaVersion'] = 0
                break
            case 'php':
                scan_app_type += ', Php'
                break
            case 'csharp':
                scan_app_type += ', CSharp'
                // Типа проекта: Solution, WebSite
                config['ProjectType'] = 'Solution'
                // Путь к файлу решения/проекта Приложение А. Пример конфигурационного файла 54
                config['SolutionFile'] = 'file'
                // Папка сайта
                config['WebSiteFolder'] = ''
                break
            case 'javascript':
                scan_app_type += ', JavaScript'
                // Путь к файлу скрипта
                config['JavaScriptProjectFile'] = 'file'
                // Путь к корневой папке проекта javascript
                config['JavaScriptProjectFolder'] = ''
                break
            default:
                // Дополнительные параметры запуска
                custom_parameters = '-l all'
                // Использовать taint-анализ
                config['UseTaintAnalysis'] = true
                // Использовать только pm-анализ
                config['UsePmAnalysis'] = true
                // Игнорировать ядра интерпретации (C#, Java, PHP) при анализе
                config['DisableInterpretCores'] = false
        }
        
        // Generic settings
        // Имя проекта
        config['ProjectName'] = "${sast_project_name}_${repo_name}_${branch_name}"
        // Язык приложения: Java, Php, Csharp, VB, ObjectiveC, CPlusPlus, Sql, Swift, Python, JavaScript, Kotlin, Go
        config['ProgrammingLanguage'] = "${sast_lang}"
        // Модули поиска уязвимостей: Php, Java, CSharp, Configuration, Fingerprint, PmTaint , Blackbox, JavaScript
        config['ScanAppType'] = "${scan_app_type}"
        // Адрес сайта
        config['Site'] = "${site}"
        // Дополнительные параметры запуска
        config['CustomParameters'] = "${custom_parameters}"
        // Использовать интеграцию с Jira
        config['UseIssueTrackerIntegration'] = false
        // Загрузить зависимости
        config['IsDownloadDependencies'] = false
        // Учитывать предыдущее сканирование
        config['ConsiderPreviousScan'] = true

        // Return resulting json
        return prettyPrint(toJson(config))
    }


    void doSastScan() {
        // Generating scanning stgaes and run in parallel mode
        // scan_map - is defined in parent class
        runParallel(genStages(scan_map)) 
    }

    Map<String, Closure> genStages(Map repos) {
        Map buildStages = [:]
        repos.each { key, value ->
            String repo = key 
            String branch = value
            def n = "${repo}@${branch}"    
            buildStages.put(n, genScanStage(repo, branch)) 
        }
        return buildStages
    }

    // Just a wrapper around parallel
    void runParallel(Map parallel) {
        script.parallel(parallel)
    }

    Closure genScanStage(String repo_name, String branch_name) {
        return {
            script.stage("SAST check for ${repo_name}@${branch_name}") {
                String settings = genSettingsJson(repo_name, branch_name)

                // Show settings json in console output
                if (is_debug) {
                    script.echo("Scan settings:\n${settings}")
                }

                List sast_workmode_sync_settings = []
                // Set the value to which the build step execution status will be set when security policy assessment is failed
                sast_workmode_sync_settings << script.failIfAstUnstable('UNSTABLE')
                if (sast_vulnerability_threshold_enabled) {
                    // Set the value to which the build step execution status will be set when errors and / or warnings occur during the AST
                    sast_workmode_sync_settings << script.failIfAstFailed('UNSTABLE')
                }

                // Setup AST report generating settings
                sast_workmode_sync_settings << script.htmlPdf(fileName: "report-${repo_name}-${branch_name}.html", filter: '', includeDfd: true, includeGlossary: true, template: 'Scan results report')

                // Setup ANT based directory path for current repo
                String repo_path = "${repo_name}/**"

                if (is_debug) {
                    script.echo("Attaching files in folder: ${repo_path}")
                }

                // Run PTAI scan
                script.ptaiAst (
                    advancedSettings: '',
                    config: script.configGlobal('ptai-scan'),
                    fullScanMode: sast_incremental,
                    scanSettings: script.scanSettingsManual(jsonPolicy: '', jsonSettings: settings ),
                    transfers: [script.transfer(excludes: sast_filter_pattern, flatten: false, includes: repo_path, patternSeparator: '[, ]+', removePrefix: '', useDefaultExcludes: true)],
                    verbose: !sast_hide_debug,
                    workMode: script.workModeSync(sast_workmode_sync_settings)
                )
            }
        }
    }

    void genSastReport() {
        // Name of directory where sast reports will be saved                                         
        String reports_dir = ".ptai"

        // Create tar.gz archive and attach it to build as an artifact
        String artifact_file_name = createReportsArtifcat(reports_dir)

        // Send email with reports as attachment if any email address was provided
        sendReportsByEmail(sast_report_email_list, artifact_file_name)
    }
}