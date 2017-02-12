package xyz.nickr.telepad.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Paginates lines or pages into a user-friendly
 * collection of pages for clicking through using
 * an inline keyboard.
 *
 * @author Nick Robson
 */
@Accessors(chain = true)
public class PaginatedData {

    @Getter @Setter private String header, footer;

    @Getter private final List<String> pages = new ArrayList<>();
    private final Map<Integer, String> cachedPages = new HashMap<>();
    @Getter private final int pageCount;

    @Getter private IntFunction<String> pageFunction;

    @Getter @Setter @NonNull private ParseMode parseMode = ParseMode.NONE;

    private InlineMenu[] menus;

    /**
     * Creates an instance using the given strings as pages.
     *
     * @param pages The pages.
     */
    public PaginatedData(List<String> pages) {
        this.pages.addAll(pages);
        this.pageCount = pages.size();
    }

    /**
     * Creates an instance using the given strings as lines on
     * pages, and splits them into pages based on the linesPerPage.
     *
     * @param lines The lines
     * @param linesPerPage The maximum number of lines per page
     */
    public PaginatedData(List<String> lines, int linesPerPage) {
        List<List<String>> partition = Partition.partition(lines, linesPerPage);

        this.pages.addAll(partition.stream().map(l -> String.join("\n", l)).collect(Collectors.toList()));
        this.pageCount = pages.size();
    }

    /**
     * Creates an instance using the given function that turns
     * a page number into a page. The number of pages is specified
     * as the second argument. The function must be able to generate
     * a page for all numbers between 0 (inclusive) and the page
     * count (exclusive).
     *
     * @param pageFunction The function
     * @param pageCount The number of pages
     */
    public PaginatedData(IntFunction<String> pageFunction, int pageCount) {
        this.pageFunction = pageFunction;
        this.pageCount = pageCount;
    }

    /**
     * Gets the nth page.
     *
     * @param page The page number
     *
     * @return The page
     */
    public String getPage(int page) {
        if (pageFunction != null) {
            return cachedPages.computeIfAbsent(page, pageFunction::apply);
        }
        return pages.get(page);
    }

    /**
     * Turns the pages into an array of {@link InlineMenu}s.
     *
     * Ready to be sent using an {@link InlineMenuMessage}.
     *
     * @return The array of menus.
     */
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
                        if (x < (y - 1)) {
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

    /**
     * Sends this page set in response to a message, starting
     * with the given page.
     *
     * @param page The page number
     * @param message The message
     *
     * @return The {@link InlineMenuMessage} sent message
     */
    public InlineMenuMessage send(int page, Message message) {
        InlineMenu menu = getInlineMenus()[page];
        Message m = message.getChat().sendMessage(SendableTextMessage.markdown("_Loading..._").replyTo(message).build());
        return menu.getMenuFor(m);
    }

}
