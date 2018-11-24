import stack.SharedStack;
import stack.StackError;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;

public class CheckList {

    public final String input;
    public final String output;
    private String line;
    private SharedStack<Tag> stack;
    private int numLine = 1;
    private int errorsInLine = 0;
    private BufferedWriter outputBuff;
    private BufferedReader inputBuff;
    private int numberOfH1s = 0;
    private SharedStack<Boolean> commentStack;
    private int totalJS = 0;
    private int totalCSS = 0;
    private final int maxJS = 5;
    private final int maxCSS = 5;
    private boolean checkedXML = false;
    private String googleAnalytics;

    private class Tag{
        private String type;

        private Tag(String type){
            this.type = type;
        }

        public int compareTo(Tag finalTag) {
            return type.compareTo(finalTag.type);
        }
    }

    public CheckList(String input, String output){
        this.input = input;
        this.output = output;
    }

    public boolean checkErrors() throws FileNotFoundException {
        try{
            openFiles();
            createStacks();
            firstOutputSentences();
            line = inputBuff.readLine();
            numLine++;
            while(line != null){
                checkLine();
                line = inputBuff.readLine();
                numLine++;
            }
            writeLastResults();
            closeFiles();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void writeLastResults() throws IOException {
        if(!checkedXML){
            outputBuff.write("Line 0: Not found XML.\r\n");
        }
        if(googleAnalytics == null){
            outputBuff.write("Line 0: Not found Google Analytics or Google Tag Manager.\r\n");
        }
    }

    private void openFiles() throws IOException {
        inputBuff = new BufferedReader(new FileReader(input));
        outputBuff = new BufferedWriter(new FileWriter(output));
    }

    private void createStacks() {
        stack = new SharedStack<Tag>();
        commentStack = new SharedStack<Boolean>();
    }

    private void closeFiles() throws IOException {
        inputBuff.close();
        outputBuff.close();
    }

    private void firstOutputSentences()  throws IOException{
        outputBuff.write("File: " + input + "\r\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        outputBuff.write("Date: " + dtf.format(now) + "\r\n");
        outputBuff.write("List of Errors:" + "\r\n");
    }

    private void checkLine() throws IOException, StackError {
        errorsInLine = 0;
        checkMalAnidada();
        checkExistsOnlyOneH1();
        checkCodigoSeguimiento();
        checkExistsLineReference();
        checkNotExistsTODO();
        checkDifferentHref();
        checkNoMoreJS();
        checkNoMoreCSS();
        //checkNotBrokenLink();
        writeError();
    }

    private void writeError() throws IOException {
        if(errorsInLine != 0){
            outputBuff.write("Line " + numLine + ": " + errorsInLine + "\r\n");
        }
    }

    private void checkMalAnidada() throws IOException, StackError {
        if(!isCommented()){
            StringTokenizer strToken = new StringTokenizer(line,">");
            while(strToken.hasMoreElements()){
                String token = strToken.nextToken();
                if(token.contains("</")){
                    try{
                        token = catchNameTag(token);
                        token += ">";
                        token = searchTag(token);
                        catchPossibleError(token);
                        stack = stack.pop();
                    } catch (StackError stackError) {
                        stackError.printStackTrace();
                    }
                } else if(token.contains("<") && !token.contains("<!") && !token.contains("/>")){

                    String nameTag = catchNameTag(token);//new StringTokenizer(token, " ").nextToken().substring(1);
                    if(needsASlash(nameTag)){

                        Tag startTag = new Tag(nameTag);
                        stack = stack.push(startTag);
                    }
                }
            }
        }
    }

    private String catchNameTag(String token) {
        int start = 0;
        while(start < token.length() && token.charAt(start) != '<'){
            start++;
        }
        int finish = start;
        while(finish < token.length() && (token.charAt(finish) != ' ')){
            finish++;
        }
        return token.substring(start + 1,finish);
    }

    private String searchTag(String line) {
        StringTokenizer strToken = new StringTokenizer(line, "<");
        while(strToken.hasMoreTokens()){
            String token = strToken.nextToken();
            if(token.contains("/")){
                return token;
            }
        }
        return null;
    }

    private boolean needsASlash(String nameTag) {
        if(nameTag.equals("meta") || nameTag.equals("link") || nameTag.equals("img") || nameTag.equals("input") || nameTag.equals("br")){
            return false;
        }
        return true;
    }

    private void catchPossibleError(String nameTag) throws StackError {
        Tag expectedTag = stack.top();
        Tag finalTag = new Tag(nameTag.substring(1, nameTag.length() -1));
        if(expectedTag.compareTo(finalTag) != 0){
            incrementErrorInLine();
        }
    }

    private void incrementErrorInLine() {
        errorsInLine++;
    }

    private void checkExistsOnlyOneH1() throws IOException {
        StringTokenizer stoken = new StringTokenizer(line);
        while(stoken.hasMoreTokens()){
            if(stoken.nextToken().contains("<h1")){
                numberOfH1s++;
                if(numberOfH1s > 1){
                    outputBuff.write("Line: " + numLine + ". More than one <h1>\r\n");
                }
            }

        }
    }

    private void checkCodigoSeguimiento() throws StackError {
        if (googleAnalytics == null && line.contains("<iframe") && !isCommented()) {
            StringTokenizer sTok = new StringTokenizer(line, " ");
            while(sTok.hasMoreTokens()){
                String item = sTok.nextToken();
                if(item.contains("GTM") || item.contains("UA")){
                    StringTokenizer sTok2 = new StringTokenizer(item, "=\"");
                    while(sTok2.hasMoreTokens()){
                        String possibleAnalyticId = sTok2.nextToken();
                        if(possibleAnalyticId.contains("GTM") || possibleAnalyticId.contains("UA")){
                            googleAnalytics = possibleAnalyticId;
                        }
                    }
                }
            }
        }

    }

    private void checkExistsLineReference() throws IOException, StackError {
        if(checkedXML && line.contains("<?xml-stylesheet") && !isCommented()){
            StringTokenizer sTok =  new StringTokenizer(line, " ");
            while(sTok.hasMoreTokens()){
                String item = sTok.nextToken();
                if(item.contains("href")){
                    if(checkXLM(item)){
                        checkedXML = true;
                    }
                }
            }
        }
    }


    private boolean checkXLM(String item) {
        StringTokenizer sTok = new StringTokenizer(line, "'");
        while(sTok.hasMoreTokens()){
            String url = sTok.nextToken();
            if(url.contains("xslejemplo.xsl")){
                return true;
            }
        }
        return false;
    }

    private void checkNotExistsTODO() throws IOException {
        if(containsTODO()){
            outputBuff.write("File: " + numLine + ". There's a TODO Comment.\r\n");
        }
    }

    private boolean containsTODO() {
        return line.contains("<!-- TODO") || line.contains("// TODO") || line.contains("/* TODO");
    }

    private void checkDifferentHref() throws StackError, IOException {
        if(line.contains("href") || line.contains("action") && !isCommented()){
            StringTokenizer stok = new StringTokenizer(line, " ");
            while(stok.hasMoreTokens()){
                String item = stok.nextToken();
                if((item.contains("href") || item.contains("action")) && hasExternalCode(item) && !item.contains("semic.es")){
                    if (!hasTargetBlank(stok)){
                        outputBuff.write("Line: " + numLine + ". Needs target=\"_blank\"\r\n");
                    }

                }
            }
        }
    }

    private boolean hasTargetBlank(StringTokenizer stok) {
        while(stok.hasMoreTokens()){
            String possibleTarget = stok.nextToken();
            if(possibleTarget.contains("target=\"_blank\"")){
                return true;
            }
        }
        return false;
    }

    private boolean hasExternalCode(String item) {
        return item.contains("https://") || item.contains("htpp://");
    }

    private boolean isCommented() throws StackError {
        StringTokenizer stok = new StringTokenizer(line, " ");
        while(stok.hasMoreTokens()){
            String item = stok.nextToken();
            if(item.contains("<!--")){
                commentStack = commentStack.push(true);
            }
            if(item.contains("-->") && !commentStack.isEmpty()){
                commentStack = commentStack.pop();
            }
        }
        return !commentStack.isEmpty();
    }

    private void checkNoMoreJS() throws StackError, IOException {
        if(line.contains("<script") && !isCommented()){
            StringTokenizer sTok = new StringTokenizer(line, " ");
            while(sTok.hasMoreTokens()){
                String item =sTok.nextToken();
                if(item.contains("src")){
                    totalJS++;
                    if(totalJS > maxJS){
                        outputBuff.write("Line " + numLine + ": has more than " + maxJS + " .js loaded.\r\n");
                    }
                }
            }
        }
    }

    private void checkNoMoreCSS() throws StackError, IOException {
        if(line.contains("<style") && !isCommented()){
            StringTokenizer sTok = new StringTokenizer(line, " ");
            while(sTok.hasMoreTokens()){
                String item = sTok.nextToken();
                if(item.contains("link")){
                    totalCSS++;
                    if(totalCSS > maxCSS){
                        outputBuff.write("Line " + numLine + ": has more than " + maxCSS + " .css loaded.\r\n");
                    }
                }
            }
        }

    }

    private void checkNotBrokenLink() throws StackError, IOException {
        if(line.contains("https:/") || line.contains("http:/") && !isCommented()){
            StringTokenizer sTok = new StringTokenizer(line, " ");
            while(sTok.hasMoreTokens()){
                String item =  sTok.nextToken();
                if(item.contains("http") && !isLive(getUrl(item))){
                    outputBuff.write("Line " + numLine + ": a broken link.\r\n");

                }
            }
        }
    }

    private String getUrl(String item) {
        StringTokenizer sTok = new StringTokenizer(line, "\"");
        while(sTok.hasMoreTokens()){
            String url = sTok.nextToken();
            if(url.contains("http")){
                return url;
            }
        }
        return null;
    }

    private static boolean isLive(String link){
        HttpURLConnection urlconn = null;
        int res = -1;
        String msg = null;
        try {
            URL url = new URL(link);
            urlconn = (HttpURLConnection)url.openConnection();
            urlconn.setConnectTimeout(10000);
            urlconn.setRequestMethod("GET");
            urlconn.connect();
            String redirlink = urlconn.getHeaderField("Location");
            System.out.println(urlconn.getHeaderFields());
            if(redirlink != null && !url.toExternalForm().equals(redirlink))
                return isLive(redirlink);
            else
                return urlconn.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(urlconn != null){
                urlconn.disconnect();
            }
            return true;
        }
    }


}
