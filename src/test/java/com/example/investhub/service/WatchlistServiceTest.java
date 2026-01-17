package com.example.investhub.service;

import com.example.investhub.exception.ResourceAlreadyExistsException;
import com.example.investhub.exception.ResourceNotFoundException;
import com.example.investhub.model.Asset;
import com.example.investhub.model.User;
import com.example.investhub.model.WatchlistEntry;
import com.example.investhub.repository.AssetRepository;
import com.example.investhub.repository.UserRepository;
import com.example.investhub.repository.WatchlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private WatchlistService watchlistService;

    private User user;
    private Asset btc;
    private Asset eth;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("test1");

        btc = new Asset();
        btc.setId(10L);
        btc.setSymbol("BTCUSDT");
        btc.setName("Bitcoin");

        eth = new Asset();
        eth.setId(11L);
        eth.setSymbol("ETHUSDT");
        eth.setName("Ethereum");
    }

    // ---------------- getUserWatchlist ----------------

    @Test
    void getUserWatchlist_returnsAssets() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));

        WatchlistEntry e1 = entry(100L, user, btc);
        WatchlistEntry e2 = entry(101L, user, eth);

        when(watchlistRepository.findByUser_Id(1L)).thenReturn(List.of(e1, e2));

        List<Asset> result = watchlistService.getUserWatchlist("test1");

        assertEquals(2, result.size());
        assertEquals("BTCUSDT", result.get(0).getSymbol());
        assertEquals("ETHUSDT", result.get(1).getSymbol());
    }

    @Test
    void getUserWatchlist_userMissing_throwsResourceNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> watchlistService.getUserWatchlist("missing"));

        assertEquals("User", ex.getResourceType());
        assertEquals("missing", ex.getIdentifier());
    }

    // ---------------- addToWatchlist ----------------

    @Test
    void addToWatchlist_createsNewEntry() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));
        when(assetRepository.findBySymbol("BTCUSDT")).thenReturn(Optional.of(btc));

        when(watchlistRepository.findByUser_Id(1L)).thenReturn(List.of()); // empty watchlist
        when(watchlistRepository.save(any(WatchlistEntry.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        WatchlistEntry saved = watchlistService.addToWatchlist("BTCUSDT", "test1");

        assertNotNull(saved);
        assertEquals(user, saved.getUser());
        assertEquals(btc, saved.getAsset());

        ArgumentCaptor<WatchlistEntry> captor = ArgumentCaptor.forClass(WatchlistEntry.class);
        verify(watchlistRepository).save(captor.capture());
        WatchlistEntry toSave = captor.getValue();

        assertEquals("BTCUSDT", toSave.getAsset().getSymbol());
        assertEquals(1L, toSave.getUser().getId());
    }

    @Test
    void addToWatchlist_userMissing_throwsResourceNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> watchlistService.addToWatchlist("BTCUSDT", "missing"));

        assertEquals("User", ex.getResourceType());
        assertEquals("missing", ex.getIdentifier());
        verify(assetRepository, never()).findBySymbol(anyString());
        verify(watchlistRepository, never()).save(any());
    }

    @Test
    void addToWatchlist_assetMissing_throwsResourceNotFound() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));
        when(assetRepository.findBySymbol("BTCUSDT")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> watchlistService.addToWatchlist("BTCUSDT", "test1"));

        assertEquals("Asset", ex.getResourceType());
        assertEquals("BTCUSDT", ex.getIdentifier());
        verify(watchlistRepository, never()).save(any());
    }

    @Test
    void addToWatchlist_whenAlreadyExists_throwsResourceAlreadyExists() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));
        when(assetRepository.findBySymbol("BTCUSDT")).thenReturn(Optional.of(btc));

        WatchlistEntry existing = entry(100L, user, btc);
        when(watchlistRepository.findByUser_Id(1L)).thenReturn(List.of(existing));

        ResourceAlreadyExistsException ex = assertThrows(ResourceAlreadyExistsException.class,
                () -> watchlistService.addToWatchlist("BTCUSDT", "test1"));

        assertEquals("Watchlist entry", ex.getResourceType());
        assertEquals("BTCUSDT", ex.getIdentifier());
        verify(watchlistRepository, never()).save(any());
    }

    // ---------------- removeFromWatchlist ----------------

    @Test
    void removeFromWatchlist_deletesEntry() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));

        WatchlistEntry existing = entry(100L, user, btc);
        when(watchlistRepository.findByUser_Id(1L)).thenReturn(List.of(existing));

        watchlistService.removeFromWatchlist("BTCUSDT", "test1");

        verify(watchlistRepository).delete(existing);
    }

    @Test
    void removeFromWatchlist_userMissing_throwsRuntime() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> watchlistService.removeFromWatchlist("BTCUSDT", "missing"));

        assertEquals("User not found", ex.getMessage());
        verify(watchlistRepository, never()).delete(any());
    }

    @Test
    void removeFromWatchlist_assetNotInWatchlist_throwsRuntime() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));

        WatchlistEntry existing = entry(100L, user, eth);
        when(watchlistRepository.findByUser_Id(1L)).thenReturn(List.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> watchlistService.removeFromWatchlist("BTCUSDT", "test1"));

        assertEquals("Asset not in watchlist", ex.getMessage());
        verify(watchlistRepository, never()).delete(any());
    }

    // ---------------- isInWatchlist ----------------

    @Test
    void isInWatchlist_returnsTrueWhenExists() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));

        WatchlistEntry existing = entry(100L, user, btc);
        when(watchlistRepository.findByUser_Id(1L)).thenReturn(List.of(existing));

        boolean result = watchlistService.isInWatchlist("BTCUSDT", "test1");
        assertTrue(result);
    }

    @Test
    void isInWatchlist_returnsFalseWhenMissing() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));

        WatchlistEntry existing = entry(100L, user, eth);
        when(watchlistRepository.findByUser_Id(1L)).thenReturn(List.of(existing));

        boolean result = watchlistService.isInWatchlist("BTCUSDT", "test1");
        assertFalse(result);
    }

    @Test
    void isInWatchlist_userMissing_throwsRuntime() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> watchlistService.isInWatchlist("BTCUSDT", "missing"));

        assertEquals("User not found", ex.getMessage());
    }

    // ---------------- helper ----------------

    private static WatchlistEntry entry(Long id, User user, Asset asset) {
        WatchlistEntry e = new WatchlistEntry();
        e.setId(id);
        e.setUser(user);
        e.setAsset(asset);
        return e;
    }
}