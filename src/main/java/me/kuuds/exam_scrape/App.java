package me.kuuds.exam_scrape;

import com.opencsv.CSVWriter;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class App {

  private String filename;
  private Question question;

  public App(String filename) {
    this.filename = filename;
  }

  @SneakyThrows
  public void build() {
    String content = read();
    parse(content);
  }

  private String read() throws IllegalAccessException {
    File file = new File(filename);
    if(!file.exists() || !file.isFile()) {
      throw new IllegalAccessException();
    }

    StringBuilder sb = new StringBuilder();
    try(FileReader in = new FileReader(file)) {
      char[] buf = new char[2048];
      int line = 0;
      while((line = in.read(buf)) > 0) {
        sb.append(buf, 0 , line);
      }
    } catch (IOException e) {
      throw new IllegalAccessException();
    }
    return sb.toString();
  }

  private void parse(String content) {
    Document doc = Jsoup.parse(content);
    String title = doc.select("#PaperName").first().text();
    List<Question> questions = new LinkedList<>();

    for (Element e : doc.body().select("[data-type=question][class=topic]")) {
      Question q = new Question();
      String question = e.select("#QTitle").text();
      String qType = e.select("#QType").text();
      List<String> options = e.select("[class=items]#options [class=item] [class=span]").stream().map(Element::text).collect(Collectors.toList());
      String answer = e.select("[class=answer] [data-type=answer]").text();

      q.setQuestion(question);
      q.setQType(qType);
      q.setOption(options);
      q.setAnswer(answer);
      questions.add(q);
    }

    generate(title, questions);
//    generate_hwp(title, questions);
  }

  public void generate(String title, List<Question> questions) {
    try (Writer writer = new FileWriter(title + ".csv");
         CSVWriter w = new CSVWriter(writer, '\t','"', '"', "\n")) {
      questions.forEach(q -> {
        String[] content = new String[2];
        StringBuilder sb = new StringBuilder();
        sb.append("<div><div><span><p>[").append(q.qType).append("]  <b>").append(q.getQuestion()).append("<b></p></span></div>");
        sb.append("<div><ul class=\"QOptions\" >");
        for (String s : q.getOption()) {
          sb.append("<li><span><p>").append(s).append("</p></span></li>");
        }
        sb.append("</ul></div></div>");
        content[0] = sb.toString();
        content[1] = q.getAnswer();
        w.writeNext(content);
      });

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void generate_hwp(String title, List<Question> questions) {
    try (Writer w = new FileWriter(title + ".txt")) {
      for(Question question : questions) {
        w.write("\r\n");
        w.write(question.getQType());
        w.write("\r\n");
        w.write(question.getQuestion());
        w.write("\r\n");
        w.write("\r\n");
        for(String option : question.getOption()) {
          w.write(option);
          w.write("\r\n");
        }
        w.write(question.getAnswer());
        w.write("\r\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    App a = new App("target/classes/exam1.html");
    a.build();
    a = new App("target/classes/exam2.html");
    a.build();
  }
}
