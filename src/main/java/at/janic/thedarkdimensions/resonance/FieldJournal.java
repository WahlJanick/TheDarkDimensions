package at.janic.thedarkdimensions.resonance;

import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

public final class FieldJournal extends Item {
    public FieldJournal(Properties p){super(p);}
    public static Component text(String key,Object...args){return Component.translatable("resonance.thedarkdimensions."+key,args);}
    public static void saveWaypoint(ServerPlayer p,TrialController trial){
        if(!trial.bounds().inflate(2).contains(p.position()))return;
        ResonanceProgress.discover(p,trial);var d=ResonanceProgress.data(p);var site=d.sites.get(trial.identity());if(site!=null){d.sites.put(trial.identity(),site.saved());p.sendSystemMessage(text("waypoint_saved"),true);}
    }
    public static void refresh(ServerPlayer p,ItemStack book){
        var data=ResonanceProgress.data(p);List<Filterable<Component>> pages=new ArrayList<>();
        int worldTrials=at.janic.thedarkdimensions.EndProgression.data(p.level()).completed;
        pages.add(Filterable.passThrough(Component.translatable("end_progression.thedarkdimensions.journal_"+(worldTrials==7?"ready":"locked"),Integer.bitCount(worldTrials))));
        var finale=at.janic.thedarkdimensions.finale.FinalProgress.data(p);pages.add(Filterable.passThrough(Component.translatable("finale.thedarkdimensions.journal_"+(finale.won?"won":finale.found?"found":data.completed==7?"seek":"hint"))));if(!finale.waypoint.isEmpty())pages.add(Filterable.passThrough(Component.literal(finale.waypoint)));
        pages.add(Filterable.passThrough(text("journal_intro",Integer.bitCount(data.completed),3)));
        TrialKind next=null;for(var kind:TrialKind.values())if((data.completed&kind.mask())==0&&data.sites.values().stream().anyMatch(s->s.kind()==kind.ordinal())){next=kind;break;}
        if(data.completed==7)pages.add(Filterable.passThrough(text("journal_next_complete")));
        else if(next!=null)pages.add(Filterable.passThrough(text("journal_next_trial",Component.translatable(next.title()),next.core().getDefaultInstance().getHoverName())));
        else{TrialKind unseen=Arrays.stream(TrialKind.values()).filter(k->(data.completed&k.mask())==0).findFirst().orElse(TrialKind.EMBER);pages.add(Filterable.passThrough(text("journal_next_explore",text("hint_"+unseen.getSerializedName()))));}
        var biomes=text("journal_biomes").copy().withStyle(ChatFormatting.BOLD);for(String id:data.biomes)biomes.append("\n").append(Component.translatable("biome.thedarkdimensions."+id));pages.add(Filterable.passThrough(biomes));
        for(var kind:TrialKind.values()){
            boolean found=data.sites.values().stream().anyMatch(s->s.kind()==kind.ordinal());boolean done=(data.completed&kind.mask())!=0;
            var page=Component.empty().append(found?Component.translatable(kind.title()).withStyle(ChatFormatting.BOLD):text("unknown"));page.append("\n\n").append(text(found?"trial_details_"+kind.getSerializedName():"hint_"+kind.getSerializedName()));
            if(found)page.append("\n\n").append(text(done?"completed":"not_completed"));pages.add(Filterable.passThrough(page));
        }
        var runes=text("journal_runes").copy();for(int n=0;n<4;n++)runes.append("\n\n").append(ResonanceContent.RUNES.get(n).get().getDefaultInstance().getHoverName()).append(": ").append(text(ResonanceProgress.unlocked(p,n)?"unlocked":"locked"));pages.add(Filterable.passThrough(runes));
        pages.add(Filterable.passThrough(text("journal_waypoints")));
        for(var site:data.sites.values().stream().filter(ResonanceProgress.Site::waypoint).limit(64).toList())pages.add(Filterable.passThrough(Component.empty().append(Component.translatable(TrialKind.values()[site.kind()].title())).append("\n\n"+site.dimension()+"\n"+site.x()+", "+site.y()+", "+site.z())));
        book.set(DataComponents.WRITTEN_BOOK_CONTENT,new WrittenBookContent(Filterable.passThrough("Ashbound Field Journal"),"The Dark Dimensions",0,pages,true));
    }
    @Override public InteractionResult use(Level l,Player player,InteractionHand hand){
        if(player instanceof ServerPlayer p){if(p.isShiftKeyDown()){var trial=TrialController.nearest(p.level(),p.blockPosition(),24);if(trial!=null)saveWaypoint(p,trial);}var book=p.getItemInHand(hand);refresh(p,book);p.inventoryMenu.broadcastChanges();p.containerMenu.broadcastChanges();p.openItemGui(book,hand);}return InteractionResult.SUCCESS;
    }
}
