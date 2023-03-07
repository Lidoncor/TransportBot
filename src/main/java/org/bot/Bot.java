package org.bot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.bot.gis.Item;
import org.bot.gis.Result;
import org.bot.gis.Route;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.*;
import java.util.*;

public class Bot extends TelegramLongPollingBot {
    final private String BOT_TOKEN = "";
    final private String BOT_NAME = "";
    final private String GIS_TOKEN = "";
    private Transport route;
    private ReplyKeyboardMarkup replyKeyboardMarkup;
    private InlineKeyboardMarkup inlineKeyboardMarkup;
    String output = "";
    String response = "";

    Bot() throws IOException {
        setReplyKeyboardMarkup();
        setInlineKeyboardMarkup();
        route = new Transport();
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    public void setInlineKeyboardMarkup() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Автобус");
        inlineKeyboardButton1.setCallbackData("Автобус");

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("Троллейбус");
        inlineKeyboardButton2.setCallbackData("Троллейбус");

        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        inlineKeyboardButton3.setText("Трамвай");
        inlineKeyboardButton3.setCallbackData("Трамвай");

        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        inlineKeyboardButton4.setText("Маршрутка");
        inlineKeyboardButton4.setCallbackData("Маршрутка");

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow1.add(inlineKeyboardButton2);

        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow2.add(inlineKeyboardButton3);
        keyboardButtonsRow2.add(inlineKeyboardButton4);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);

        inlineKeyboardMarkup.setKeyboard(rowList);
    }

    public void setReplyKeyboardMarkup() {
        replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        KeyboardButton replyKeyboardButton = new KeyboardButton("Отправить геолокацию");
        replyKeyboardButton.setRequestLocation(true);


        KeyboardButton replyKeyboardButton2 = new KeyboardButton("Отменить");

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(replyKeyboardButton);
        keyboardRow.add(replyKeyboardButton2);

        ArrayList<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(keyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message inMess = update.getMessage();
                SendMessage outMess = new SendMessage();
                outMess.setChatId(inMess.getChatId().toString());

                if (inMess.hasText() && inMess.getText().equals("Отменить")) {
                    route.number = null;
                    route.type = null;
                    response = "";
                    output = "";
                    outMess.setText("Вы отменили ввод");
                    execute(outMess);
                } else if (inMess.hasText() && route.number == null) {
                    try {
                        route.number = Integer.parseInt(inMess.getText());
                    } catch (Exception e) {
                        outMess.setText("Необходимо ввести номер маршрута");
                        execute(outMess);
                        return;
                    }
                    outMess.setText("Выберите тип транспорта");
                    outMess.setReplyMarkup(inlineKeyboardMarkup);
                    execute(outMess);
                } else if (inMess.hasText() && route.type == null) {
                    outMess.setText("Необходимо выбрать тип транспорта");
                    outMess.setReplyMarkup(inlineKeyboardMarkup);
                    execute(outMess);
                } else if (inMess.hasText() && route.number != null && route.type != null) {
                    outMess.setText(response);
                    outMess.setReplyMarkup(replyKeyboardMarkup);
                    execute(outMess);
                } else if (inMess.hasLocation() && route.number != null && route.type != null) {
                    Location location = inMess.getLocation();

                    double longitude = location.getLongitude(); // 82.8085065831518 заменить на location.getLongitude()
                    double latitude = location.getLatitude(); // 54.98845307336007 заменить на location.getLatitude()

                    //вместо файла data.json
                    //String sURL = "https://catalog.api.2gis.com/3.0/items?q=остановка общественного транспорта&type=station&subtype=stop&fields=items.routes&point="+longitude+","+latitude+"&key=" + GIS_TOKEN;
                    //URL url = new URL(sURL);
                    //URLConnection request = url.openConnection();
                    //request.connect();


                    JsonReader reader = new JsonReader(new FileReader("src/main/resources/data.json")); //убрать
                    JsonParser parser = new JsonParser();

                    //JsonElement root = parser.parse(new InputStreamReader((InputStream) request.getContent()));

                    JsonElement root = parser.parse(reader); //убрать

                    JsonObject rootObj = root.getAsJsonObject();
                    JsonElement result = rootObj.get("result");

                    Gson gson = new Gson();
                    Result resultObj = gson.fromJson(result, Result.class);

                    GraspParams params = new GraspParams();
                    params.m = route.number;
                    if (route.type.contains("Автобус")) {
                        params.t = 1;
                        route.gisType = "bus";
                    } else if (route.type.contains("Троллейбус")) {
                        params.t = 2;
                        route.gisType = "trolleybus";
                    } else if (route.type.contains("Трамвай")) {
                        params.t = 3;
                        route.gisType = "tram";
                    } else if (route.type.contains("Маршрутка")) {
                        params.t = 8;
                        route.gisType = "shuttle_bus";
                    }

                    Instant instant = Instant.ofEpochSecond(inMess.getDate());
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.from(ZonedDateTime.now()));

                    Calendar c = Calendar.getInstance();
                    c.setTime(Date.from(instant));
                    int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                    dayOfWeek -= 1; // делаем -1 потому что java начинает с вскр
                    if (dayOfWeek >= 1 || dayOfWeek <= 5) {
                        params.sch = 11;
                    } else params.sch = 5;

                    ArrayList<String> userStops = new ArrayList<>();
                    for (Item i : resultObj.items) {
                        for (Route r : i.routes) {
                            if (Objects.equals(r.name, route.number.toString()) && Objects.equals(r.subtype, route.gisType)) {
                                userStops.add(i.name);
                                break;
                            }
                        }
                    }
                    if (userStops.size() == 0) {
                        outMess.setText("Маршрут не найден");
                        execute(outMess);
                        output = "";
                        route.type = null;
                        route.number = null;
                        response = "";
                        return;
                    }

                    Document doc = Jsoup.connect("https://nskgortrans.ru/components/com_planrasp/helpers/grasp.php?tv=mr&m=" + params.m + "&t=" + params.t + "&r=B&sch=" + 23 + "&s=0&v=0").get();
                    Element mainTable;

                    Map<Integer, ArrayList<Integer>> timeTable;

                    String direction = "";
                    mainTable = doc.select("table").first();

                    if (mainTable.childNodes().get(1).childNodes().get(2).childNodes().get(1).childNodes().get(0).toString().equals("")) {


                        doc = Jsoup.connect("https://nskgortrans.ru/components/com_planrasp/helpers/grasp.php?tv=mr&m=" + params.m + "&t=" + params.t + "&r=B&sch=" + params.sch + "&s=0&v=0").get();
                        mainTable = doc.select("table").first();
                        direction = mainTable.childNodes().get(1).childNodes().get(0).childNodes().get(1).childNodes().get(3).childNodes().get(0).toString();
                        for (String userStop : userStops) {
                            timeTable = extractTimeTable(doc, userStop);
                            printAll(direction, userStop, timeTable, zonedDateTime);
                        }

                        doc = Jsoup.connect("https://nskgortrans.ru/components/com_planrasp/helpers/grasp.php?tv=mr&m=" + params.m + "&t=" + params.t + "&r=A&sch=" + params.sch + "&s=0&v=0").get();
                        mainTable = doc.select("table").first();
                        direction = mainTable.childNodes().get(1).childNodes().get(0).childNodes().get(1).childNodes().get(3).childNodes().get(0).toString();
                        for (String userStop : userStops) {
                            timeTable = extractTimeTable(doc, userStop);
                            printAll(direction, userStop, timeTable, zonedDateTime);
                        }


                    } else {
                        mainTable = doc.select("table").first();
                        direction = mainTable.childNodes().get(1).childNodes().get(0).childNodes().get(1).childNodes().get(3).childNodes().get(0).toString();
                        for (String userStop : userStops) {
                            timeTable = extractTimeTable(doc, userStop);
                            printAll(direction, userStop, timeTable, zonedDateTime);
                        }

                        doc = Jsoup.connect("https://nskgortrans.ru/components/com_planrasp/helpers/grasp.php?tv=mr&m=" + params.m + "&t=" + params.t + "&r=A&sch=" + 23 + "&s=0&v=0").get();
                        mainTable = doc.select("table").first();
                        direction = mainTable.childNodes().get(1).childNodes().get(0).childNodes().get(1).childNodes().get(3).childNodes().get(0).toString();
                        for (String userStop : userStops) {
                            timeTable = extractTimeTable(doc, userStop);
                            printAll(direction, userStop, timeTable, zonedDateTime);
                        }

                    }

                    outMess.setText(output);
                    execute(outMess);

                    output = "";
                    route.type = null;
                    route.number = null;
                    response = "";
                }

            } else if (update.hasCallbackQuery()) {
                AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(update.getCallbackQuery().getId());

                SendMessage outMess = new SendMessage();
                outMess.setChatId(update.getCallbackQuery().getMessage().getChatId());

                route.type = update.getCallbackQuery().getData();

                response += "Транспорт: " + route.number + " " + route.type + "\n";
                response += "Теперь отправьте геолокацию";

                outMess.setText(response);
                outMess.setReplyMarkup(replyKeyboardMarkup);

                execute(answerCallbackQuery);
                execute(outMess);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printAll(String direction, String userStop, Map<Integer, ArrayList<Integer>> timeTable, ZonedDateTime zonedDateTime) {
        if (timeTable == null) return;

        output += direction + "\n";
        output += "\t" + userStop + "\n";
        output += "\t" + "Ближ:";

        ArrayList<Integer> hours = new ArrayList<>();
        hours.add(zonedDateTime.getHour());
        hours.add(zonedDateTime.getHour() + 1);

        Integer count;
        for (Integer h : hours) {
            if (timeTable.containsKey(h)) {
                count = 0;
                for (Integer i : timeTable.get(h)) {
                    if (count.equals(2)) break;

                    if (i < 10) output += " " + h + ":" + 0 + i;
                    else output += " " + h + ":" + i;

                    count++;
                }
            }

        }
        output += "\n\n";
    }

    public Element searchUserStop(Elements busStops, String userStop) {
        if (userStop.contains(" ")) {
            String[] userStopSplit = userStop.split(" ");
            Integer count = 0;
            for (Element bs : busStops) {
                for (String t : userStopSplit) {
                    if (bs.childNodes().get(0).toString().contains(t)) {
                        count++;
                        if (count.equals(userStopSplit.length)) {
                            return bs;
                        }
                    }
                }
            }
        } else {
            for (Element bs : busStops) {
                if (bs.childNodes().get(0).toString().contains(userStop)) {
                    return bs;
                }
            }
        }
        return null;
    }

    public Map<Integer, ArrayList<Integer>> extractTimeTable(Document doc, String userStop) {

        Elements busStops = doc.select("h2"); // все остановки
        Element foundUserStop = searchUserStop(busStops, userStop);

        if (foundUserStop == null) return null;

        int userStopIndex = foundUserStop.siblingIndex();
        Element table = (Element) foundUserStop.parent().childNodes().get(userStopIndex + 3);
        List<Node> tbody = table.childNodes().get(1).childNodes();

        Map<Integer, ArrayList<Integer>> timeTable = new HashMap<>();
        Integer lastHourValue = null;
        for (Node tr : tbody) {
            try {
                List<Node> td = tr.childNodes();
                for (Node data : td) {
                    try {
                        if (data.attributes().get("class").equals("td_plan_n") || data.attributes().get("class").equals("td_plan_h")) {
                            lastHourValue = Integer.parseInt(data.childNodes().get(1).childNodes().get(0).toString());
                            timeTable.put(lastHourValue, new ArrayList<>()); // убрать new array для пустых часов
                        } else if (data.attributes().get("class").equals("td_plan_m")) {
                            List<Node> minutes = data.childNodes();
                            for (Node m : minutes) {
                                try {
                                    String s = m.childNodes().get(0).toString().replaceAll("[^\\d.]", "");
                                    timeTable.get(lastHourValue).add(Integer.parseInt(s));
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return timeTable;
    }
}
