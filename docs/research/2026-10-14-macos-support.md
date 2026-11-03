# Оценка поддержки macOS

Дата: 14 октября 2026. База: исходники 0.1.3. Статус — анализ;
macOS target не получен, сборка/установка/GUI на macOS не выполнялись.
Код, ограничения совместимости и Windows-поставка не изменялись.

Последующая реализация: по новому запросу пользователя в **0.1.5** разрешён
macosx/x86_64 с Java 17 поверх актуальной палитры 0.1.4. Windows-сборка и регрессия
проверены. Затем пользователь подтвердил работу на Mac: «проверено, работает»;
конфигурация и отдельные сценарии не сообщены. Ниже сохранён первоначальный
анализ базы 0.1.3; актуальное состояние — [проверки 0.1.5](../testing/macos-validation.md).

## Вывод

Адаптация технически возможна, ожидаемый объём — небольшой или умеренный.
Большинство продуктового кода уже использует Java и публичные Eclipse/SWT API.
Разработка, unit tests и сборка p2 ZIP могут выполняться на Windows.
Mac-дистрибутив не является обязательной зависимостью для сборки нашего Java
bundle. Кандидат можно собрать с текущим Windows target после расширения
проверки совместимости. SWT для macOS предоставляет установленная Mac EDT.
Полноценная runtime/визуальная приёмка требует macOS с целевой EDT — локально
или на доступном удалённом Mac. Смена osgi.os не превращает Windows в Cocoa runtime.

Официальная [страница загрузки EDT](https://edt.1c.ru/docs/new/download.php)
содержит macOS-дистрибутив именно 2026.1.3 и вариант для offline установки.
В [инструкции, связанной с этой страницей](https://dl04.1c.ru/content/DevelopmentTools/2026_1_0/runningedt.html)
описана x86 EDT и установка на Apple Silicon через Rosetta 2.
Следовательно, первый целевой вариант — macosx/cocoa/x86_64: Intel Mac либо
Apple Silicon с x86 EDT/JVM через Rosetta. Нельзя автоматически объявлять
поддержку нативной aarch64 EDT; она требует отдельной подтверждённой поставки.

## Найденные ограничения в проекте

| Место | Текущее состояние | Что требуется |
|---|---|---|
| EdtCompatibility.verify | Явно допускает только win32/x86_64 и Java 17 | Добавить проверенную Mac-конфигурацию, сохранив контроль версии |
| Root pom.xml | Разрешает зависимости для win32/win32/x86_64 | Mac environment/profile нужен для дополнительной проверки Mac dependency resolution, не обязательный шаг сборки Java-кандидата |
| targets/edt-2026.1.target | Directory установленной Windows EDT и Windows environment | Можно использовать для сборки кандидата; отдельный Mac target нужен для проверки Mac-зависимостей на build host |
| build.ps1 | Windows Maven/JDK executable paths | Для сборки кандидата на Windows менять не требуется |
| p2-test / prepare-test-edt | Windows пути, layout копии и p2.os/ws/arch | Отдельная поддержка Mac layout и provisioning parameters |
| run-workbench-probe | Java.exe и Windows EDT ini/layout | Mac запуск с соответствующими launcher/ini/JVM args и изоляцией тестовых каталогов |
| ReadableCombo | Собственный Shell/Table, focus, Deactivate, EraseItem/PaintItem | Проверить Cocoa popup, выделение, Escape/Enter, клик, геометрию и ресурсы |
| Шрифты / preview | Публичные FontRegistry/FontData API, список реально доступных шрифтов | Проверить Mac font metrics, исходный font override/его отсутствие и restart |

В manifest/feature собственных bundles не найдено platform filter или native
library declarations. В исходниках продукта не найдено SWT internal/Win32/JNI/JNA
использования или запуска внешних OS-команд. Плагин не поставляет свои DLL/dylib.
Это аргументы за переносимость, но не runtime-доказательство.

Существующий ZIP не следует называть поддерживающим Mac: даже если p2 сможет его
установить, EdtCompatibility не разрешит работу на macOS.

## Что можно сделать на Windows

1. Расширить platform guard для нужной Mac-конфигурации, сохранив проверки версии
   Java и bundles. Это единственный найденный явный runtime-блок ОС в продукте.
2. Собрать Java bundle/feature и p2 ZIP текущими Windows-инструментами/target,
   выполнить unit tests и передать кандидат коллеге с уже установленной EDT
   **2026.1.3**. Отдельно скачивать Mac-дистрибутив для этого не требуется.
3. При необходимости дополнительно проверить Mac dependency resolution на Windows:
   получить common/Cocoa bundles нужной версии, добавить Mac environment/target.
   Только для этого нужен архив target или официальный p2 repository.
   Недостаточно добавить macosx environment поверх target только с Windows SWT.
4. Manifests, CSS/resources и preference IDs Mac можно исследовать из архива,
   но сведения о версиях и runtime-проверки также может предоставить коллега.
   Совпадение карты Windows/Mac пока не подтверждено.

[Tycho target-platform configuration](https://tycho.eclipseprojects.io/doc/4.0.8/target-platform-configuration/target-platform-configuration-mojo.html)
документирует environments os/ws/arch, включая macosx/cocoa/x86_64 и aarch64.
Это механизм выбора целевых зависимостей, не эмулятор другой ОС. В проекте
используется Tycho 4.0.13; параметры существующей конфигурации проверены по POM.
Страница 4.0.13 при исследовании не открылась, поэтому приведена документация
4.0.8 того же механизма, а не заявление о выполненной Mac-сборке 4.0.13.

При совпадении API и mappings Windows/Mac возможно выпускать один универсальный
ZIP только с собственными Java bundles. Платформенные SWT компоненты предоставляет
сама EDT. Раздельные ZIP не обязательны только из-за различия ОС; это нужно
подтвердить dependency resolution и установкой в обе целевые платформы.

## Что требует Mac

- Установка в отдельную копию EDT/чистую тестовую workspace и p2 uninstall/audit.
- Применение Darcula/Monokai в реальных окнах, BSL editor, новых окнах и после restart.
- Возврат точных исходных absent/explicit values и сохранение внешних ручных правок.
- Реальные focus/selection, custom popup Table, закрытие по клику/Deactivate,
  keyboard navigation, bounds и Retina/DPI. Windows pixel-результаты не переносить.
- Выбор/возврат шрифтов, metrics и native font representation на Mac. Не переносить
  Windows test workspace с оригиналами/журналами на Mac: baseline должен быть Mac.
- Проверка native цветов и platform CSS. Не обещать, что системные macOS title/menu
  элементы будут выглядеть как Windows или полностью окрашиваться нашими ролями.
- Для будущего редактора палитры — native ColorDialog и live preview на Cocoa.
  Этот редактор пока не реализован в текущем чате.

При прямом Java запуске SWT на Mac учитывать JVM startup/main-thread требования
и настройки штатного launcher. [SWT FAQ](https://eclipse.dev/eclipse/swt/faq.html)
и [Eclipse launch API](https://help.eclipse.org/latest/topic/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/launching/IJavaLaunchConfigurationConstants.html)
описывают Mac first-thread режим; тестовый runner должен использовать подходящие
Mac ini/arguments, а не дословно Windows-команду.

Можно оставаться за Windows-компьютером и использовать удалённый Mac для тестов,
либо передать ZIP и проверочную матрицу пользователю Mac. Без доступа к macOS
можно подготовить кандидат на поддержку, но не объявить её проверенной.

## Объём и следующий шаг

Не требуется переписывать каталог палитр, snapshots или preferences специально
для ОС. Первое изменение — compatibility guard и сборка кандидата на Windows.
Отдельный target и перенос тестового harness нужны для расширенной автоматической
проверки; возможные Cocoa UI fixes определяются по результатам запуска на Mac.
Точная оценка времени до этого запуска была бы предположением.
Предварительная сложность низкая/умеренная, при выявлении Cocoa defects вырастет.

Практический первый шаг — получить сведения о Mac (Intel/Apple Silicon,
macOS version, EDT/JVM runtime architecture), расширить guard и собрать кандидат
на Windows без обязательного получения Mac target. Провести проверку установки,
обеих палитр, popup и восстановления на Mac; по её результатам оценивать полную
приёмку. Дополнительное ПО пользователь устанавливает сам, ничего не установлено.

Источники просмотрены 14.10.2026. Наличие Mac-дистрибутива подтверждено по странице
1С, но его содержимое и точные bundle versions пока не исследованы. Поддержка
macOS не добавлена в продукт и не включена автоматически в согласованную
доработку редактора палитры.

Уточнение после замечания коллег пользователя: первоначальный анализ излишне
жёстко представил Mac target как условие подготовки кандидата. Это условие только
дополнительной проверки Mac-зависимостей на Windows; для самого Java bundle
обязательным не является. Если у коллеги уже есть EDT нужной версии на Mac,
для первоначального теста достаточно передать собранный p2 ZIP с изменённым guard.
