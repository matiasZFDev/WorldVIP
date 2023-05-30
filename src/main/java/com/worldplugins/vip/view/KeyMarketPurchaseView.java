package com.worldplugins.vip.view;

import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemTransformer;
import com.worldplugins.lib.view.ConfigContextBuilder;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.market.SellingKey;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.key.VipKeyGenerator;
import com.worldplugins.vip.manager.PointsManager;
import com.worldplugins.vip.util.BukkitUtils;
import com.worldplugins.vip.util.VipDuration;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.NumberFormats;
import me.post.lib.util.Scheduler;
import me.post.lib.util.Time;
import me.post.lib.view.View;
import me.post.lib.view.Views;
import me.post.lib.view.action.ViewClick;
import me.post.lib.view.action.ViewClose;
import me.post.lib.view.context.ClickHandler;
import me.post.lib.view.context.ViewContext;
import me.post.lib.view.context.impl.MapViewContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

import static com.worldplugins.vip.Response.respond;
import static java.util.Objects.requireNonNull;
import static me.post.lib.util.Pairs.to;

public class KeyMarketPurchaseView implements View {
    public static class Context {
        private final @NotNull SellingKey key;
        private final @NotNull KeyMarketView.Context marketContext;

        public Context(@NotNull SellingKey key, @NotNull KeyMarketView.Context marketContext) {
            this.key = key;
            this.marketContext = marketContext;
        }
    }

    private final @NotNull ViewContext viewContext;
    private final @NotNull MenuModel menuModel;
    private final @NotNull SellingKeyRepository sellingKeyRepository;
    private final @NotNull Scheduler scheduler;
    private final @NotNull PointsManager pointsManager;
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull VipKeyGenerator vipKeyGenerator;
    private final @NotNull ConfigModel<VipData> vipConfig;

    public KeyMarketPurchaseView(
        @NotNull MenuModel menuModel,
        @NotNull SellingKeyRepository sellingKeyRepository,
        @NotNull Scheduler scheduler,
        @NotNull PointsManager pointsManager,
        @NotNull ValidKeyRepository validKeyRepository,
        @NotNull VipKeyGenerator vipKeyGenerator,
        @NotNull ConfigModel<VipData> vipConfig
    ) {
        this.viewContext = new MapViewContext();
        this.menuModel = menuModel;
        this.sellingKeyRepository = sellingKeyRepository;
        this.scheduler = scheduler;
        this.pointsManager = pointsManager;
        this.validKeyRepository = validKeyRepository;
        this.vipKeyGenerator = vipKeyGenerator;
        this.vipConfig = vipConfig;
    }

    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        final Context context = (Context) requireNonNull(data);
        final SellingKey key = context.key;
        final VipData.VIP configVip = vipConfig.data().getById(key.vipId());

        ConfigContextBuilder.withModel(menuModel)
            .replaceMenuItem("Key", item ->
                ItemTransformer.of(configVip.item())
                    .display(item.getItemMeta())
                    .loreFormat(
                        to("@vip", configVip.display()),
                        to("@tempo", VipDuration.format(key)),
                        to("@tipo", key.vipType().getName().toUpperCase()),
                        to("@vendedor", BukkitUtils.getPlayerName(key.sellerId())),
                        to("@usos", String.valueOf(key.vipUsages())),
                        to("@preco", NumberFormats.suffixed(key.price()))
                    )
                    .colorMeta()
                    .transform()
            )
            .handleMenuItemClick(
                "Cancelar",
                click -> Views.get().open(player, KeyMarketView.class, context.marketContext)
            )
            .handleMenuItemClick("Confirmar", click -> handleKeyPurchase(click, context))
            .build(viewContext, player, data);
    }

    private void handleKeyPurchase(@NotNull ViewClick click, @NotNull Context context) {
        final Player player = click.whoClicked();

        sellingKeyRepository.getAllKeys().thenAccept(keys -> scheduler.runTask(0, false, () -> {
            if (!player.isOnline()) {
                return;
            }

            final SellingKey key = context.key;
            final boolean hasKey = keys.stream().anyMatch(current ->
                current.code().equals(key.code())
            );

            if (!hasKey) {
                respond(player, "Mercado-key-inexistente");
                Views.get().open(player, KeyMarketView.class, context.marketContext);
                return;
            }

            if (!pointsManager.has(player.getUniqueId(), key.price())) {
                respond(player, "Comprar-key-cash-insuficiente");
                return;
            }

            final ValidVipKey newValidKey = new ValidVipKey(
                player.getName(),
                vipKeyGenerator.generate(),
                key.vipId(),
                key.vipType(),
                key.vipDuration(),
                key.vipUsages()
            );

            validKeyRepository
                .getKeyByCode(newValidKey.code())
                .thenAccept(validKey -> scheduler.runTask(0, false, () -> {
                    if (!player.isOnline()) {
                        return;
                    }

                    if (validKey != null) {
                        respond(player, "Key-gerada-duplicada");
                        Views.get().open(player, KeyMarketView.class, context.marketContext);
                        return;
                    }

                    final VipData.VIP configVip = vipConfig.data().getById(key.vipId());
                    final Player seller = Bukkit.getPlayer(key.sellerId());
                    final double points = key.price();

                    pointsManager.withdraw(player.getUniqueId(), points);
                    pointsManager.deposit(key.sellerId(), points);
                    sellingKeyRepository.removeKey(key);
                    validKeyRepository.addKey(newValidKey);

                    player.closeInventory();
                    respond(player, "Key-comprada", message -> message.replace(
                        to("@vip", configVip.display()),
                        to("@tempo", VipDuration.format(key)),
                        to("@tipo", key.vipType().getName().toUpperCase()),
                        to("@vendedor", BukkitUtils.getPlayerName(key.sellerId())),
                        to("@usos", String.valueOf(key.vipUsages())),
                        to("@preco", NumberFormats.suffixed(points)),
                        to("@key", newValidKey.code())
                    ));

                    if (seller != null) {
                        final int postTimeElapsed = (int) TimeUnit
                            .NANOSECONDS
                            .toSeconds(System.nanoTime() - key.postTimestamp());
                        System.out.println(postTimeElapsed);
                        respond(seller, "Key-vendida", message -> message.replace(
                            to("@vip", configVip.display()),
                            to("@tempo", VipDuration.format(key)),
                            to("@tipo", key.vipType().getName().toUpperCase()),
                            to("@comprador", player.getName()),
                            to("@usos", String.valueOf(key.vipUsages())),
                            to("@preco", NumberFormats.suffixed(points)),
                            to("@duracao-postagem", Time.toFormat(postTimeElapsed))
                        ));
                    }
            }));
        }));
    }

    @Override
    public void onClick(@NotNull ViewClick click) {
        ClickHandler.handleTopNonNull(viewContext, click);
    }

    @Override
    public void onClose(@NotNull ViewClose close) {
        viewContext.removeViewer(close.whoCloses().getUniqueId());
    }
}
