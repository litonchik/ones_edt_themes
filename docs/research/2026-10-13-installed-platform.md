# Проверка установленной EDT 2026.1.3

Дата: 13 октября 2026 года. Проверка чтением файлов и `javap`; EDT не запускалась, настройки не менялись.

Каталог: `C:\Program Files\1C\1CE\components\1c-edt-2026.1.3+25-x86_64`.

## Java и версии платформы

| Компонент | Подтверждённое значение | Локальный источник |
|---|---|---|
| Eclipse Platform | `4.30.0` | `.eclipseproduct` |
| Требование launcher | `osgi.requiredJavaVersion=17` | `1cedt.ini` |
| Execution environment Eclipse Platform | `JavaSE-17` | `plugins/org.eclipse.platform_4.30.0.v20231201-0110/META-INF/MANIFEST.MF` |
| CSS theme bundle | `0.14.200.v20230913-1724`, `JavaSE-17` | `org.eclipse.e4.ui.css.swt.theme` manifest |
| SWT | `3.124.200.v20231113-1355` | Имя bundle в `plugins` |
| Xtext UI | `2.33.0.v20231121-0955` | Manifest `org.eclipse.xtext.ui` |
| BSL UI | `23.0.1.v202609041212` | Manifest `com._1c.g5.v8.dt.bsl.ui` |
| EDT theming UI | `2.1.0.v202609041212`, capability JavaSE 17 | Manifest `com._1c.g5.v8.dt.theming.ui` |
| Установленный JDK 17 | Axiom `17.0.18+10-LTS`, x86_64 | `C:\Program Files\Axiom\AxiomJDK-Pro-17-Full/release`, запуск `bin/java.exe -version` |

Есть `java.exe` и `javac.exe` в `C:\Program Files\Axiom\AxiomJDK-Pro-17-Full\bin`. В PATH по умолчанию обнаружен JDK 25, но это не версия целевой платформы EDT.

**Решение для проекта:** использовать установленный JDK 17 для разработки/проверки, выпускать байткод Java 17 и указывать `JavaSE-17`. Maven/Tycho выбирать совместимыми с этим JDK и target Eclipse 4.30. Текущий пример вендора с Java 25 / EDT 2026.2 не подходит как готовое окружение.

Значения `requiredJavaVersion` и execution environment подтверждают требование Java 17, но сами по себе не доказывают запрет запуска на всех более новых Java. Совместимость EDT с JDK 25 не проверялась и не принимается за рабочую основу.

В `1cedt.ini` нет явного `-vm`; по нему нельзя установить фактически выбранную JVM. На момент проверки процессы `1cedt`, `1cedtc`, `1cedtcli` не найдены. Настройки EDT Start не исследовались; глобальные PATH/JAVA_HOME и параметры запуска не изменялись.

## Подтверждённая особенность тёмной темы

В классе `com._1c.g5.v8.dt.theming.ui.util.DtThemeUtil` метод `isDarkThemeActive()` сравнивает текущий CSS theme ID с точной строкой:

`org.eclipse.e4.ui.css.theme.e4_dark`.

Метод `getIconPath(String)` использует результат этого сравнения для выбора пути иконки. Это установлено чтением байткода `javap -c -p`, без вызова методов EDT.

В `plugin.xml` bundles BSL UI, theming UI, common UI, properties UI и других CSS contributions тёмного оформления также привязаны к стандартному dark ID. Собственная тема с другим ID не получает эти contributions автоматически и не распознаётся этим методом как тёмная.

**Уточнение архитектуры:** для MVP предпочтительны профили Darcula/Monokai, применяемые при активной штатной Dark EDT. Выбор профиля — на собственной странице плагина; стандартный селектор Eclipse показывает Dark. Палитры меняют именованные цвета/настройки, используемые штатным CSS. Дополнительные CSS-правила допустимы только при доказанной необходимости и с проверкой порядка применения. Публичный механизм удаления/замены динамически подключённого stylesheet ещё не проверен.

Самостоятельные CSS theme IDs остаются возможным последующим вариантом, но требуют отдельного решения привязки adaptations и dark icons. Одного импорта dark CSS недостаточно.

## BSL: реальные настройки цветов

Источник: `com._1c.g5.v8.dt.bsl.ui_23.0.1.v202609041212.jar`, файл `css/dark/edt-dark_preferencestyle.css`.

CSS selector: `IEclipsePreferences#com-_1c-g5-v8-dt-bsl-ui`. Предполагаемый preference node — `com._1c.g5.v8.dt.bsl.ui`; применение через API и scope ещё нужно проверить.

Префикс ключей: `com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.`.

| Суффикс ключа | Значение штатного dark CSS |
|---|---|
| `Builtin function.color` | `255,140,110` |
| `BSL_Keywords.color` | `255,120,90` |
| `Comment.color` | `120,136,147` |
| `Operators.color` | `181,181,181` |
| `Others.color` | `181,181,181` |
| `Preprocessor.color` | `160,202,244` |
| `Brackets.color` | `181,181,181` |
| `Strings.color` | `240,255,125` |
| `Numbers.color` | `181,181,181` |

`javap -constants` дополнительно подтвердил style IDs `BSL_Pragmas` и `Label`. Их preference keys не следует объявлять проверенными только по аналогии с таблицей.

Настройки общего редактора в theming CSS включают фон/текст, флаги SystemDefault, выделение, текущую строку и номера строк. Реестр цветов `org.eclipse.ui.themes` в BSL bundle содержит цвета подсказок/ссылок; токены кода в проверенном файле используют отдельные Xtext preferences. Это подтверждает необходимость адаптера BSL.

## Что ещё не проверено

- Изменение и сохранение палитры на реальном экране EDT.
- Порядок применения CSS preferences, default scope и instance scope; не перезаписывает ли штатная темизация выбранную палитру.
- Полный набор цветов EDT: часть может регистрироваться программно.
- Перезапуск, возврат исходных значений, отмена, p2 установка/удаление.
- Maven/Tycho/PDE: установка и сборка ещё не выполнялись.

Результат подтверждает статические контракты и уточняет дизайн, но не подтверждает готовность плагина.
