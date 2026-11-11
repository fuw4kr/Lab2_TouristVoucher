# Лабораторна робота №2. XML, XSD, XSL — варіант 13 «Туристичні путівки»

Java-застосунок для роботи з XML-документом, що описує туристичні путівки агентства.

Що робить програма:

- **валідує** `tourist-voucher.xml` за схемою `tourist-voucher.xsd`;
- **розбирає** документ парсерами **DOM**, **SAX** та **StAX** і створює колекцію об'єктів `Voucher`;
- **сортує** колекцію за обраним критерієм (інтерфейс `Comparator`);
- **перетворює** документ за допомогою `tourist-voucher.xsl`: список путівок розбивається
  на підсписки за типом (`excursion`, `pilgrimage`, `rest`, `weekend`);
- веде **журнал** роботи (SLF4J + Logback).

## Опис документа

Кореневий елемент `TouristVoucher` містить елементи `Voucher`:

| Елемент | Зміст | Тип у XSD |
|---------|-------|-----------|
| `Voucher/@id` | ідентифікатор путівки | `xs:ID` з шаблоном `v-NNN`, обов'язковий атрибут |
| `Type` | тип путівки | перелік `weekend`, `excursion`, `rest`, `pilgrimage` |
| `Country` | країна подорожі | рядок 2–60 символів з великої літери |
| `NumberDaysNights` | кількість днів і ночей | атрибути `days` (1–90) та `nights` (0–90) |
| `Transport` | вид транспорту | перелік `air`, `railway`, `auto`, `liner` |
| `HotelCharacteristic/Stars` | кількість зірок | ціле 1–5 |
| `HotelCharacteristic/Meal` | харчування | перелік `none`, `BB`, `HB`, `AI` |
| `HotelCharacteristic/RoomType` | тип номера (кількість місць) | ціле 1–3 |
| `HotelCharacteristic/Tv`, `AirConditioner` | телевізор, кондиціонер | `xs:boolean` |
| `Cost/@currency` | валюта | перелік `UAH`, `USD`, `EUR` |
| `Cost/Total` | вартість путівки | додатне число, 2 знаки після коми |
| `Cost/Service` | що включено і скільки коштує | текст + атрибут `price` (`simpleContent`) |

У схемі використано прості та комплексні типи, переліки, граничні значення,
обов'язкові атрибути та тип `ID`.

## Будова коду

| Пакет | Призначення |
|-------|-------------|
| `com.touragency` | `TouristVoucherApp` (точка входу), `AppOptions` (параметри запуску) |
| `model` | `Voucher` та його складові: `StayDuration`, `HotelCharacteristic`, `Cost`, `IncludedService`, переліки |
| `parser` | парсери DOM / SAX / StAX і спільний для них код |
| `validation` | `XsdValidator` — перевірка за схемою |
| `transform` | `XsltTransformer` — XSL-перетворення |
| `sorting` | `VoucherOrder`, `CostComparator` — сортування |
| `xml` | `SafeXml` — налаштування JAXP (захист від XXE), `VoucherXmlException` |

Щоб не дублювати код між трьома парсерами:

- `VoucherTag` — таблиця «елемент XML → що він заповнює у путівці», спільна для всіх парсерів;
- `VoucherBuilder` — єдине місце перетворення рядків на числа, прапорці та переліки;
- `VoucherEventCollector` — збирає путівки з подій, його використовують і SAX, і StAX;
- `VoucherReader` — робота з файлом, логування та обробка помилок для будь-якого парсера.

## Як запустити

Потрібна JDK 17 або новіша (Maven завантажиться сам через Maven Wrapper).

```bash
./mvnw verify
```

```bash
./mvnw exec:java
```

У Windows — `mvnw.cmd verify` та `mvnw.cmd exec:java`.

Параметри (усі необов'язкові):

| Параметр | Значення за замовчуванням |
|----------|---------------------------|
| `--xml=<файл>` | `src/main/resources/tourist-voucher.xml` |
| `--xsd=<файл>` | `src/main/resources/tourist-voucher.xsd` |
| `--xsl=<файл>` | `src/main/resources/tourist-voucher.xsl` |
| `--out=<файл>` | `output/vouchers-by-type.xml` |
| `--sort=<порядок>` | `country`; також `type`, `duration`, `stars`, `cost` |

```bash
./mvnw exec:java -Dexec.args="--sort=cost --out=output/by-type.xml"
```

## Логування

Конфігурація — `src/main/resources/logback.xml`. У консоль потрапляють повідомлення від `INFO`,
у файл `logs/tourist-voucher.log` — від `DEBUG`; файл щодня замінюється новим, історія зберігається 14 днів.
Під час тестів діє `logback-test.xml`, який пише лише в консоль.

## Тестування

Тести — JUnit 5, покриття — JaCoCo (`target/site/jacoco/index.html`).
Команда `./mvnw verify` не пройде, якщо тестами покрито менше 95 % рядків.
