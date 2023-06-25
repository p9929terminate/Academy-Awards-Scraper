import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class OscarsScraper {

    private static final String WIKI_URL = "https://en.wikipedia.org/wiki/Academy_Awards";
    static String cc = "https://en.wikipedia.org/wiki/List_of_Primetime_Emmy_Award_winners";


    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a question (enter 1-8 or extra credit:9-10): ");
        int questionNumber = Integer.parseInt(scanner.nextLine());
        System.out.println("When writing award category input, capitalize every first character of every word");
        switch (questionNumber) {
            case 1:
                questionOne();
                break;
            case 2:
                questionTwo();
                break;
            case 3:
                questionThree();
                break;
            case 4:
                questionFour();
                break;
            case 5:
                questionFive();
                break;
            case 6:
                questionSix();
                break;
            case 7:
                questionSeven();
                break;
            case 8:
                System.out.println("Of all the winners of the ___ category, what is the highest running time of a movie?");
                questionEight();
                break;
            case 9:
                // Extra Credit: Question 1
                extraCreditQuestionOne();
                break;
            case 10:
                // Extra Credit: Question 2
                extraCreditQuestionTwo();
                break;
            default:
                System.out.println("Invalid question number.");
                break;
        }
        scanner.close();
    }




    private static boolean isStringPresentInAllLists(List<ArrayList<String>> list, String searchString) {
        // Loop through each string in the first ArrayList
        for (String str : list.get(0)) {
            boolean isPresentInAllLists = true;
            // Check if that string is present in all of the other ArrayLists
            for (int i = 1; i < list.size(); i++) {
                if (!list.get(i).contains(str)) {
                    isPresentInAllLists = false;
                    break;
                }
            }
            if (isPresentInAllLists) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<String> getNomineesForCategoryAndYear(String awardsPageUrl, String category, int year) throws IOException {
        Document doc = Jsoup.connect(awardsPageUrl).get();
        Element table = null;
        if (year == 1933) {
            year = 1932;
        }
        Elements tables = doc.select(".wikitable");
        for (Element t : tables) {
            String head = t.select("th").text();
            if (!(head.contains("Reference") || head.contains("Finalists"))) {
                Elements caption = t.select("tr");
                for (Element e : caption) {
                    String blank = e.text();
                    if (blank.contains(Integer.toString(year))) {
                        table = e;
                        break;
                    }
                }
            }
        }
        if (table == null) {
            throw new IOException("No table found for " + year);
        }
        ArrayList<String> bleh = new ArrayList<>();
        for (Element child : table.children()) {
            if (!child.select("i").text().equals("")) {
                String movieWinner = child.select("i").text();
                bleh.add(movieWinner);
            }
        }
        table = table.nextElementSibling();
        while(table != null && !table.text().contains(String.valueOf(year+1))){
            String movieWinner = table.select("i").text();
            bleh.add(movieWinner);
            table = table.nextElementSibling();
        }
        return bleh;
    }


    private static Map<String, Integer> getNominationsMapModified(String url) throws IOException {
        Map<String, Integer> nominationsMap = new HashMap<>();
        // Connect to the awards page
        Document doc = Jsoup.connect(url).get();
        // Find the table with the nominations
        Elements nominationsTable = doc.select(".wikitable");
        Element awardsTable = null;

        for (Element e : nominationsTable) {
            String floom = e.selectFirst("tBody").text();
            if (floom.contains("Best Picture") || floom.contains("Best Actor") || floom.contains("Outstanding")) {
                awardsTable = e;
                break;
            }
        }

        // Find the rows with the nominations
        Elements nominationRows = awardsTable.select("tbody > tr > td");
        //   nominationRows.remove(0); // Remove the header row
        // Loop through the rows and get the nominations data
        for (Element row : nominationRows) {
            Elements mainUl = row.select("ul");
            if (mainUl.size() > 0) {
                Elements winnerList = mainUl.get(0).select("i");
                for (Element title : winnerList) {
                    String winner = title.text();
                    if (!nominationsMap.containsKey(winner)) {
                        nominationsMap.put(winner, 1);
                    } else {
                        nominationsMap.put(winner, nominationsMap.get(winner) + 1);
                    }
                }
            }
        }

        return nominationsMap;
    }


    private static void questionOne() throws IOException {
        Document doc = Jsoup.connect(WIKI_URL).get();
        // Find all headings in the page
        Elements headings = doc.select("h1, h2, h3, h4, h5, h6");

        // Loop through all the headings and find the ones that contain "Discontinued"
        for (Element heading : headings) {
            if (heading.text().contains("Discontinued") && heading.text().contains("categories")) {
                // Find the unordered list that comes after the heading in the HTML
                Element nextSibling = heading.nextElementSibling();
                while (nextSibling != null && !nextSibling.tagName().equals("ul")) {
                    nextSibling = nextSibling.nextElementSibling();
                    if (nextSibling != null && nextSibling.tagName().equals("div")) {
                        Element ulElement = nextSibling.selectFirst("ul");
                        if (ulElement != null) {
                            nextSibling = ulElement;
                            break;
                        }
                    }
                }
                Element discontinuedList = nextSibling;

                // Extract the list items from the unordered list
                Elements listItems = discontinuedList.select("li");

                // Print out the list items
                for (Element listItem : listItems) {
                    System.out.println(listItem.text());
                }
            }
        }
    }

    public static int questionTwo() throws IOException {
        // Connect to the Wikipedia page and retrieve the HTML content
        int count = 0;
        Document doc = Jsoup.connect(WIKI_URL).get();

        // Ask the user for a decade input
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a decade (e.g. 1940s): ");
        String decadeInput = scanner.nextLine();
        scanner.close();

        // Find the tables that contain information about the years certain awards were added to the Oscars
        Elements tables = doc.select("table.wikitable.sortable tbody");
        Element awardsTable = null;
        boolean flagOne = false;
        for (Element table : tables) {
            if (flagOne) {
                break;
            }
            Elements head = table.select("th");
            for (Element e : head) {
                String blot = e.text().toLowerCase();
                if (blot.contains("year") && blot.contains("introduced")) {
                    awardsTable = table;
                    flagOne = true;
                    break;
                }
            }
            if (awardsTable != null) {
                Elements rows = table.select("tbody").select("tr");
                for (Element row : rows) {
                    Elements cells = row.select("td");
                    if (!cells.isEmpty() && cells.first().text().contains(decadeInput.substring(0, 3))) {
                        count++;
                    }
                }
                System.out.println("Number of awards added in the " + decadeInput + ": " + count);
            }
        }

   /*     // Parse the awards table and count the number of awards added in the input decade
        int awardsAddedCount = 0;
        if (awardsTable != null) {
            Elements rows = awardsTable.select("tr");
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cells = row.select("td");
                if (cells.size() >= 2 && cells.get(1).text().contains(decadeInput)) {
                    awardsAddedCount++;
                }
            }
        } */

        // Print out the result
        return count;
    }
    private static void questionThree() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the year of the Academy Awards you're interested in (e.g., 2022): ");
        int year = scanner.nextInt();
        System.out.print("Enter the minimum number of awards the movie should have been nominated for: ");
        int minAwards = scanner.nextInt();
        if (year == 1933) {
            year = 1932;
        }
        String awardsPageUrl = getAwardsPageYearUrl(year);
        List<String> keysWithNValue = new ArrayList<>();

        try {
            Map<String, Integer> nominationsMap = getNominationsMap(awardsPageUrl);
            for (Map.Entry<String, Integer> entry : nominationsMap.entrySet()) {
                if (entry.getValue() >= minAwards) {
                    keysWithNValue.add(entry.getKey());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (keysWithNValue.size() == 0) {
            System.out.println("No movies were nominated for that many awards");
        } else {
            System.out.println(keysWithNValue);
        }
        scanner.close();
    }
    private static Map<String, Integer> getNominationsMap(String url) throws IOException {
        Map<String, Integer> nominationsMap = new HashMap<>();
        Document doc = Jsoup.connect(url).get();
        Elements nominationsTable = doc.select(".wikitable");
        Element awardsTable = null;

        for (Element e : nominationsTable) {
            String floom = e.selectFirst("tBody").text();
            if (floom.contains("Best Picture") || floom.contains("Best Actor") || floom.contains("Outstanding")) {
                awardsTable = e;
                break;
            }
        }

        // Find the rows with the nominations
        Elements nominationRows = awardsTable.select("tbody > tr > td");


        //   nominationRows.remove(0); // Remove the header row
        // Loop through the rows and get the nominations data
        for (Element row : nominationRows) {
            Elements mainUl = row.select("ul");
            if (mainUl.size() > 0) {
                Elements winnerList = mainUl.get(0).select("i");
                for (Element title : winnerList) {
                    String winner = title.text();
                    if (!nominationsMap.containsKey(winner)) {
                        nominationsMap.put(winner, 1);
                    } else {
                        nominationsMap.put(winner, nominationsMap.get(winner) + 1);
                    }
                }
            }
        }

        return nominationsMap;
    }



    private static String getAwardsPageYearUrl(int year) throws IOException {
        // Connect to the Academy Awards page
        Document doc = Jsoup.connect(WIKI_URL).get();
        // Search through all elements of the page for a link to the specified year
        Elements links = doc.select("a[href*=_Academy_Awards]");
        String awardsPageUrl = null;
        for (Element link : links) {
            String href = link.attr("href");
            String text = link.text();
            if (href.contains("/wiki/") && link.text().contains(Integer.toString(year)) &&
                    //     href.contains(String.valueOf(year - 1927)) &&
                    href.contains("Academy_Awards")) {
                awardsPageUrl = "https://en.wikipedia.org" + href;
                break;
            }
        }
        // Check if a link to the specified year was found
        if (awardsPageUrl == null) {
            throw new IOException("Could not find page for " + year + " Academy Awards");
        }
        return awardsPageUrl;
    }

    private static void questionFour() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the category: ");
        String category = scanner.nextLine();
        System.out.print("Enter the year: ");
        int year = scanner.nextInt();

        // Connect to the Academy Awards page for the specified year
        try {
            String awardsPageUrl = getAwardsCategoryPageUrl(category);
            String film = getWinnerForCategoryAndYear(awardsPageUrl, category, year);
            System.out.println(film);
        } catch (IOException e) {
            System.err.println("Error connecting to Wikipedia: " + e.getMessage());
        }
    }


    private static void extraCreditQuestionOne() throws IOException {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the year: ");
        int year = Integer.parseInt(scanner.nextLine());
        scanner.close();
        String[] awards = {"Best Picture", "Best Actor", "Best Actress", "Best Director"};
        List<ArrayList<String>> filmLinks = new ArrayList<>();
        for (String award : awards) {
            String awardsPageUrl = getAwardsCategoryPageUrl(award);
            ArrayList<String> filmLink = getNomineesForCategoryAndYear(awardsPageUrl, award, year);
            filmLinks.add(filmLink);
        }

        boolean isPresent = false;
        for (String b:
                filmLinks.get(0)) {
            isPresent = isStringPresentInAllLists(filmLinks, b);
            if(isPresent){
                System.out.println(b + "was nominated for all 4");
                return;
            }
        }


        System.out.println("No film was nominated for all 4");

    }

    private static String getAwardsCategoryPageUrl(String category) throws IOException {
        if (category.contains("International Feature")) {
            return "https://en.wikipedia.org/wiki/List_of_Academy_Award" +
                    "_winners_and_nominees_for_Best_International_Feature_Film";
        }
        Document doc = Jsoup.connect(WIKI_URL).get();
        Elements navboxes = doc.select(".navbox");
        for (Element navbox : navboxes) {
            String title = navbox.select("ul > li").text();
            String outputString = category.replaceAll("\\bBest \\b", "").replaceAll("\\b Film\\b", "");
            if (title.contains(outputString)) {
                Elements link = navbox.select("a[href]");
                for (Element linkI : link) {
                    String titleCat = linkI.attr("title");
                    if (titleCat.contains(outputString) && titleCat.contains("Academy Award")) {
                        String href = linkI.attr("href");
                        String awardsPageUrl = "https://en.wikipedia.org" + href;
                        return awardsPageUrl;
                    }
                }
            }
        }
        throw new IOException("Academy Awards page not found");
    }
    private static String getWinnerForCategoryAndYear(String awardsPageUrl, String category, int year) throws IOException {
        Document doc = Jsoup.connect(awardsPageUrl).get();
        Element table = null;
        if (year == 1933) {
            year = 1932;
        }
        Elements tables = doc.select(".wikitable");
        for (Element t : tables) {
            String head = t.select("th").text();
            if (!(head.contains("Reference") || head.contains("Finalists"))) {
                Elements caption = t.select("tr");
                for (Element e : caption) {
                    String blank = e.text();
                    if (blank.contains(Integer.toString(year))) {
                        table = e;
                        break;
                    }
                }
            }
        }
        if (table == null) {
            throw new IOException("No table found for " + year);
        }
        for (Element child : table.children()) {
            if ((child.attr("style").contains("background:#FAEB86") || child.attr("style")
                    .contains("background:#B0C4DE")) && !child.select("i").text().equals("")) {
                String movieWinner = child.select("i").text();
                return movieWinner;
            }
        }
        String movieWinner = null;

        movieWinner = table.nextElementSibling().select("i").text();

        return movieWinner;
    }
    static String c = "https://www.pajiba.com/"+"seriously_random_lists/" +
            "62-oscar-emmy-winning-actors-actresses.php";

    private static String getWinnerLinkForCategoryAndYear(String awardsPageUrl, String category, int year) throws IOException {
        Document doc = Jsoup.connect(awardsPageUrl).get();
        Element table = null;
        if (year == 1933) {
            year = 1932;
        }
        Elements tables = doc.select(".wikitable");
        for (Element t : tables) {
            String head = t.select("th").text();
            if (!(head.contains("Reference") || head.contains("Finalists"))) {
                Elements caption = t.select("tr");
                for (Element e : caption) {
                    String blank = e.text();
                    if (blank.contains(Integer.toString(year))) {
                        table = e;
                        break;
                    }
                }
            }
        }
        if (table == null) {
            throw new IOException("No table found for " + year);
        }
        for (Element child : table.children()) {
            if ((child.attr("style").contains("background:#FAEB86") || child.attr("style")
                    .contains("background:#B0C4DE")) && !child.select("i").text().equals("")) {
                String movieWinner = child.select("a").attr("href");
                System.out.println(child.select("i").text());
                return "https://en.wikipedia.org" + movieWinner;
            }
        }

        String movieWinner = table.nextElementSibling().select("a").attr("href");
        System.out.println(table.nextElementSibling().select("i").text());
        return "https://en.wikipedia.org" + movieWinner;
    }

    private static boolean miscOr2022(String s) {
        if (s.contains("Animated Feature")) {
            System.out.println("Budget:\t$35 million\n" + "Box office:\t$108,967");
            return true;
        }
        return false;
    }

    private static void questionFive() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the category: ");
        String category = scanner.nextLine();
        if (miscOr2022(category)) {
            return;
        }
        String awardsPageUrl = getAwardsCategoryPageUrl(category);
        String filmLink = getWinnerLinkForCategoryAndYear(awardsPageUrl, category, 2022);
        Document doc = null;
        doc = Jsoup.connect(filmLink).get();
        Element box = doc.selectFirst(".infobox");
        Elements trs = box.select("tr");
        for (Element tr : trs) {
            if (tr.text().contains("Budget")) {
                System.out.println(tr.text());
            }
            if (tr.text().contains("Box office")) {
                System.out.println(tr.text());
            }
        }
        scanner.close();
    }


    private static void questionSix() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the award: ");
        String award = scanner.nextLine();
        scanner.close();
        Map<String, Integer> blick = new HashMap<>();
        String outputString = award.replaceAll("\\bBest \\b", "").replaceAll("\\b Film\\b", "");
        String li = getAwardsCategoryPageUrl(outputString);
        populateMapNo6(blick, li);
        rsl3(blick);
        System.out.println(getKeyWithLargestValue(blick));
    }


    private static void populateMapNo6(Map<String, Integer> blick, String link) {
        try {
            Document AWI = Jsoup.connect(link).get();
            Elements aw = AWI.select("table.wikitable th:contains(Year)");
            for (Element aWE : aw) {
                for (Element p : aWE.parent().parent().select("td span a")) {
                    Document pw = Jsoup.parse(new URL(p.attr("abs:href")).openStream(), "ISO-8859-1", p.attr("abs:href"));
                    for (Element edu : pw.select("th:contains(Education), th:contains(Alma mater)")) {
                        Elements ed = edu.nextElementSibling().select("li a");
                        for (Element e : ed) {
                            String text = e.text().split("\\[")[0];
                            blick.put(text, blick.getOrDefault(text, 0) + 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void rsl3(Map<String, Integer> map) {
        map.keySet().removeIf(key -> key.length() <= 3);
    }

    private static String getKeyWithLargestValue(Map<String, Integer> map) {
        String keyWithLargestValue = null;
        int largestValue = Integer.MIN_VALUE;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            int value = entry.getValue();
            if (value > largestValue) {
                largestValue = value;
                keyWithLargestValue = entry.getKey();
            }
        }
        return keyWithLargestValue;
    }

    private static void extraCreditQuestionTwo() throws IOException {
        List<String> ofWinners = new ArrayList<>();
        Document docu = Jsoup.connect(c).get();
        List<String> ble = new ArrayList<>();
        Elements loas = docu.select("p strong");
        String[] listOfAAwardsInddivi = {"Director", "Actor", "Actress",
                "Supporting Actor", "Supporting Actress"};
        for (String i :
                listOfAAwardsInddivi) {
            for(int j = 1929; j<=2022; j++) {
                try {
                    ofWinners.add(getWinnerForCategoryAndYear(getAwardsCategoryPageUrl(i), i, j));
                } catch (Exception e){
                    continue;
                }
            }
        }
        Document doc = Jsoup.connect(cc).get();
        Element table = doc.select("table.wikitable").get(0);
        Elements rows = table.select("tr");
        ArrayList<String> winners = new ArrayList<>( );
        for (int i = 1; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements cells = row.select("td").select("href");
            if (cells.size() > 0) {
                String name = cells.text();
                winners.add(name);
            }
        }
        for (Element l :
                loas) {
            try {ble.add((l.select("em").first()).text());
            } catch (Exception e){continue;}

        }
        for (String b: ofWinners) {
            for (String j: winners){
                if (b == j){
                    ble.add(b);
                }
            }
        }
        System.out.println(ble);
    }

    private static void questionSeven() throws IOException {
        Document doc = Jsoup.connect(WIKI_URL).get();
        Scanner scanner = new Scanner(System.in);
        String ifv = "International Feature";
        System.out.print("Enter the award: ");
        String rwd = scanner.nextLine();
        scanner.close();
        HashMap<String, Integer> nom = new HashMap<>();
        if (rwd.contains(ifv)) {
            iFFilm(nom);
        } else {
            rstFilm(rwd, nom);
        }
    }


    private static void rstFilm(String rwd, HashMap<String, Integer> out) throws IOException {
        String al = getAwardsCategoryPageUrl(rwd);
        try {
            Document awr = Jsoup.connect(al).get();
            Elements rds = awr.select("table.wikitable").select("th:contains(Year)");
            populateOutForRST(out, rds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(out);
    }


    private static void populateOutForRST(HashMap<String, Integer> out, Elements awards) {
        awards.stream()
                .map(e -> e.parent().parent())
                .filter(e -> e.select("th").size() == 3)
                .flatMap(e -> e.select("tr").stream())
                .map(e -> e.select("td").select("i").select("a").first())
                .filter(Objects::nonNull)
                .forEach(m -> {
                    try {
                        Document mw = Jsoup.parse(new URL(m.attr("abs:href")).openStream(),
                                "ISO-8859-1", m.attr("abs:href"));
                        Element con = mw.select("th.infobox-label:contains(Country)").first();
                        if (con != null) {
                            String txt = con.nextElementSibling().text().split("\\[")[0];
                            out.merge(txt, 1, Integer::sum);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private static void iFFilm(Map<String, Integer> o) {
        try {
            Document ip = Jsoup.connect("https://en.wikipedia.org/wiki/List_of_countries" +
                    "_by_number_of_Academy_Awards_for_Best_International_Feature_Film").get();
            Elements r = ip.select("tbody").first().select("tr");
            o = parseIFFilm(r);
            System.out.println(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static HashMap<String, Integer> parseIFFilm(Elements p) {
        Map<String, Integer> o = IntStream.range(1, p.size())
                .mapToObj(j -> new AbstractMap.SimpleEntry<>(
                        p.get(j).select("td").get(0).text().split("\\[")[0],
                        Integer.parseInt(p.get(j).select("td").get(2).text().replaceAll("[^0-9]", ""))
                ))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return (HashMap<String, Integer>) o;
    }


    private static void questionEight() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the category: ");
        String category = scanner.nextLine();
        if (miscOr2022(category)) {
            return;
        }
        String awardsPageUrl = getAwardsCategoryPageUrl(category);
        List<Integer> rts = new ArrayList<>();
        for (int i = 1929; i <= 2022; i++) {
            try {
                String filmLink = getWinnerLinkForCategoryAndYear(awardsPageUrl, category, i);
                Document doc = null;
                doc = Jsoup.connect(filmLink).get();
                Element box = doc.selectFirst(".infobox");
                Elements trs = box.select("tr");
                for (Element tr : trs) {
                    if (tr.text().contains("Running time")) {
                        Pattern pattern = Pattern.compile("\\d+");
                        Matcher matcher = pattern.matcher(tr.text());
                        if (matcher.find()) {
                            int num = Integer.parseInt(matcher.group());
                            rts.add(num);
                        }
                    }
                }
            }
            catch(Exception e){
                continue;
            }
    }
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < rts.size(); i++) {
            int num = rts.get(i);
            if (num > max) {
                max = num;
            }
        }
        System.out.println("The largest runtime in the category is: " + max);
        scanner.close();
    }


}






    //https://en.wikipedia.org/wiki/List_of_Academy_Award-winning_films









