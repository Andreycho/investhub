package com.example.investhub.service;

import com.example.investhub.exception.ResourceNotFoundException;
import com.example.investhub.model.User;
import com.example.investhub.repository.HoldingRepository;
import com.example.investhub.repository.TransactionRepository;
import com.example.investhub.repository.UserRepository;
import com.example.investhub.repository.WatchlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private HoldingRepository holdingRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private WatchlistRepository watchlistRepository;

    @InjectMocks private AccountService accountService;

    @Test
    void resetAccount_shouldDeleteUserDataAndResetBalance() {
        User user = new User();
        user.setId(10L);
        user.setUsername("test1");
        user.setUsdBalance(new BigDecimal("123.45"));

        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));

        var response = accountService.resetAccount("test1");

        verify(holdingRepository).deleteByUser_Id(10L);
        verify(transactionRepository).deleteByUser_Id(10L);
        verify(watchlistRepository).deleteByUser_Id(10L);
        verify(userRepository).save(user);

        assertEquals(new BigDecimal("30000.00"), user.getUsdBalance());

        assertNotNull(response);
        assertEquals(new BigDecimal("30000.00"), response.getUsdBalance());
        assertEquals("Account reset successful", response.getMessage());

        verifyNoMoreInteractions(holdingRepository, transactionRepository, watchlistRepository);
    }

    @Test
    void resetAccount_whenUserNotFound_shouldThrowResourceNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.resetAccount("missing")
        );

        assertEquals("User", ex.getResourceType());
        assertEquals("missing", ex.getIdentifier());

        verifyNoInteractions(holdingRepository, transactionRepository, watchlistRepository);
        verify(userRepository, never()).save(any());
    }
}