/*

 Если необходимо отредактировпать данные транспондеров для спутников (например, в имидже содержатся устаревшие данные), необходимо зайти на Dreambox через любой FTP-клиент. Для этих целей можно воспользоваться, например - программой DreamBoxEdit ("FTP -> Receive Files from Dreambox") ("FTP -> Получение файлов из Dreambox"). При FTP подключении к ресиверу используются по умолчанию логин: root и пароль: dreambox. Также, для ресиверов на прошивках на базе Enigma2, в настройках программы DreamBoxEdit необходимо будет выбрать "Новые Enigma2 установки". Для редактирования данного файла необходимо скачать файл satellites.xml (может находиться по адресу /etc/satellites.xml либо /etc/tuxbox/satellites.xml).

 На примере рассмотрим небольшой отрезок файла satellites.xml, который содержит в себе следующие строчки:

 <satellites>
 <sat name="E036.0 NTV + Eutelsat W4" flags="1" position="361">
 <transponder frequency="11727000" symbol_rate="27500000" polarization="0" fec_inner="4"/>
 <transponder frequency="12073000" symbol_rate="27500000" polarization="0" fec_inner="3"/>
 <transponder frequency="12111000" symbol_rate="27500000" polarization="0" fec_inner="3"/>
 <transponder frequency="12178000" symbol_rate="4340000" polarization="0" fec_inner="3"/>
 ...
 <transponder frequency="12476000" symbol_rate="27500000" polarization="1" fec_inner="3"/>
 </sat>
 </satellites>

 <satellites> ... </satellites> - данные тэги используются для указания границ, в диапазоне которых указываются параметры всех спутников.
 <sat> ... </sat> - данные тэги используются для указания границ, в диапазоне которых указываются параметры каждого конкретного спутника, в нашем случае:
 name="E036.0 NTV + Eutelsat W4" - указывает название спутника;
 flags="1" - означает использование сетевого поиска при сканировании каналов;
 position="361" - указывает на орбитальную позицию спутника - 36.1E, для спутников расположенных в западной долготе геостационарной орбиты применяется знак «-» перед позицией спутника.

 Для каждого спутника можно использовать flags:
 1 -> Network Scan - сетевой поиск каналов, через анализ информации передаваемой с каждого транспондера;
 2 -> use BAT - Bouquet Association Table – поиск каналов через таблицу групповой принадлежности, передаваемую в потоке;
 4 -> use ONIT - Original Network Information Table – поиск каналов через оригинальную сетевую таблицу, транслируемую в потоке.
 Также можно использовать различные комбинации флагов, если указать значение их суммы, например - flag=1+4=5, Dreambox будет использовать при сканировании транспондеров Network Scan и ONIT, если они присутствуют в потоке.

 <transponder...>  - в этом тэге прописываются параметры транспондера:
 frequency="12073000" - частота транспондера, имеет значение 12,073 GHz;
 symbol_rate="27500000" - скорость потока передаваемых данных - 27500 Bit/s;
 polarization="2" - поляризация транслируемого сигнала, 0=горизонтальная линейная, 1=вертикальная линейная, 2=левая круговая, 3=правая круговая;
 fec_inner="3" - избыточная плотность передаваемых данных для коррекции возникаемых ошибок, 1=FEC 1/2, 2=FEC 2/3, 3=FEC 3/4, 4=FEC 5/6, 5= FEC 7/8;
 system="1" - стандарт передачи данных с транспондера, 0=DVB-S, 1=DVB-S2;
 modulation="2" - применяемый тип модуляции, 1=QPSK, 2=8PSK.
 Параметы system и modulation могут отсутствовать.

 */



import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import java.io.*;


public class Main {


    private static void addToFile(String myString) {
        // file
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("satellites.xml", true));
            out.write(myString + "\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        String url = "http://www.lyngsat.com/Eutelsat-36A-36B.html";
        String prov = "Tricolor TV";
        Document doc = Jsoup.connect(url).get();
        Elements tables = doc.select("table");
        String sTmp = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n" +
                "<!-- \n" +
                "     file generated on thursday, 18th of december 2014, 05:44:24 [GMT] \n" +
                "     by online satellites.xml generator @ http://satellites-xml.eu \n" +
                "     please let us know if you find any inconsistencies in this file \n" +
                "-->\n" +
                "<satellites>\n" +
                "\t<sat name=\"Eutelsat 36A/36B (36.0E)\" flags=\"0\" position=\"360\">";
        addToFile(sTmp);
       // int colTabl=0;
        for (Element table:tables) {

            int tableRows = table.select("tr").size();   //количество строк
             Elements rows = table.select("tr");
             for (Element row: rows) {
                 if (tableRows > 10 && tableRows < 150) {   // если строк в таблице меньше 10 - то это какието мелкие таблицы ненужные, отсекаем и есил болье 150 это скорее всего будет вся таблица целиком
                     Elements cells = row.getElementsByTag("td");  // берем количество ячеек в строке
                     if (cells.size()>7) {                         // если их больше 7 то это нужна таблица, в которой хранятся нужные данные

                         String nameProv = cells.get(2).text();    // именно в этой ячейки хранится имя провайдера телевидения
                         if (prov.equals(nameProv)) {              // сравниваем с нужным, почемуто не сработалоа простое prov==nameProv пришлось через equals
                             String tmpString = cells.get(0).text().trim();     // в этой ячейки частота и полярзация
                             int ss = tmpString.indexOf(' ', 0);        // ищем первый пробел, в строке 11655 V fgfgfd первый пробел должен быть в 5, но указывает на 7, есть очучение что цифры игнорит
                             String speed =cells.get(5).text();          // в эту переменную скорость и fec, fec не стал распарсивать ибо для моего провайдера он одинаковый
                             String pol = tmpString.substring(ss-1,ss);

                             String strOut = "\t\t<transponder frequency=\"" + tmpString.substring(0,ss-2) + "000\"" + " symbol_rate=\"" + speed.substring(0,5) + "000\"";

                             if (pol.equals("L")) pol="0"; else pol="1";        // поляризация, если L - то это 0, если V то 1, нужно для ресиверв в цифровом формате, на сайте буквы
                             strOut=strOut + " polarization=\""+pol + "\" fec_inner=\"3\"/>";

                             System.out.println(strOut);
                             addToFile(strOut);

                         }
                     }

                   }
                 }
             }
        sTmp = "\t</sat>\n" +
                "</satellites>\n" +
                "\n";
        addToFile(sTmp);
    }

}

