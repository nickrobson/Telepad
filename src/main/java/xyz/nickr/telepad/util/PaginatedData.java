package xyz.nickr.telepad.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import xyz.nickr.telepad.menu.InlineMenu;
import xyz.nickr.telepad.menu.InlineMenuMessage;

/**
 * @author Nick Robson
 */
@Accessors(chain = true)
public class PaginatedData {

    @Getter @Setter private String header, footer;

    @Getter private final List<String> pages = new ArrayList<>();
    @Getter private final int pageCount;

    @Getter private IntFunction<String> pageFunction;

    @Getter @Setter @NonNull private ParseMode parseMode = ParseMode.NONE;

    private InlineMenu[] menus;

    public PaginatedData(List<String> pages) {
        this.pages.addAll(pages);
        this.pageCount = pages.size();
    }

    public PaginatedData(List<String> lines, int linesPerPage) {
        List<List<String>> partition = Partition.partition(lines, linesPerPage);

        this.pages.addAll(partition.stream().map(l -> String.join("\n", l)).collect(Collectors.toList()));
        this.pageCount = pages.size();
    }

    public PaginatedData(IntFunction<String> pageFunction, int pageCount) {
        this.pageFunction = pageFunction;
        this.pageCount = pageCount;
    }

    public String getPage(int page) {
        if (pageFunction != null) {
            return pageFunction.apply(page);
        }
        return pages.get(page);
    }

    public InlineMenu[] getInlineMenus() {
        if (this.menus != null)
            return this.menus;

        InlineMenu[] menus = new InlineMenu[this.pageCount];
        for (int i = 0, j = menus.length; i < j; i++) {
            final int x = i, y = j;
            menus[i] = InlineMenu.builder()
                    .text(() -> (header != null ? header + "\n" : "") + getPage(x) + (footer != null ? "\n" + footer : ""))
                    .parseMode(parseMode)
                    .disableWebPreview(true)
                    .newRow(row -> {
                        if (x > 0) {
                            row.newButton(button -> button
                                    .text("«")
                                    .callback((m, u) -> {
                                        m.setMenu(menus[x - 1]);
                                        return null;
                                    }));
                        }
                        row.newButton(button -> button.text("Page " + (x + 1) + " of " + y));
                        if (x < y - 1) {
                            row.newButton(button -> button
                                    .text("»")
                                    .callback((m, u) -> {
                                        m.setMenu(menus[x + 1]);
                                        return null;
                                    }));
                        }
                    })
                    .build();
        }
        return this.menus = menus;
    }

    public InlineMenuMessage send(int page, Message message) {
        InlineMenu menu = getInlineMenus()[page];
        Message m = message.getChat().sendMessage(SendableTextMessage.markdown("_Loading..._").replyTo(message).build());
        return menu.getMenuFor(m);
    }

}
