package me.kuuds.exam_scrape;

import lombok.Data;

import java.util.List;

@Data
public class Question {

  String question;
  String qType;
  List<String> option;
  String answer;

}
