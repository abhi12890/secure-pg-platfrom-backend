package com.securepg.config;

import com.securepg.entity.*;
import com.securepg.repository.GuestConcernRepository;
import com.securepg.repository.RoomRepository;
import com.securepg.repository.MonthlyInvoiceRepository;
import com.securepg.repository.SystemReminderRepository;
import com.securepg.repository.TenantProfileRepository;
import com.securepg.repository.UserRepository;
import com.securepg.service.FieldEncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final TenantProfileRepository tenantProfileRepository;
    private final MonthlyInvoiceRepository monthlyInvoiceRepository;
    private final SystemReminderRepository systemReminderRepository;
    private final GuestConcernRepository guestConcernRepository;
    private final FieldEncryptionService fieldEncryptionService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile("dev")
    CommandLineRunner seedDemoData() {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            userRepository.save(User.builder()
                    .email("owner@securepg.local")
                    .passwordHash(passwordEncoder.encode("Owner@Secure123"))
                    .role(Role.OWNER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build());

            User guestUser = userRepository.save(User.builder()
                    .email("guest@securepg.local")
                    .passwordHash(passwordEncoder.encode("Guest@Secure123"))
                    .role(Role.GUEST)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build());

            Room room101 = roomRepository.save(Room.builder()
                    .roomNumber("101")
                    .capacity(2)
                    .currentOccupantsCount(1)
                    .build());

            roomRepository.save(Room.builder()
                    .roomNumber("102")
                    .capacity(2)
                    .currentOccupantsCount(0)
                    .build());

            TenantProfile tenant = tenantProfileRepository.save(TenantProfile.builder()
                    .user(guestUser)
                    .room(room101)
                    .encryptedName(fieldEncryptionService.encrypt("Aarav Sharma"))
                    .encryptedPhone(fieldEncryptionService.encrypt("+919876543210"))
                    .encryptedIdProof(fieldEncryptionService.encrypt("XXXX-XXXX-1234"))
                    .checkInDate(LocalDate.now().minusMonths(2))
                    .monthlyRentRate(new BigDecimal("12000.00"))
                    .status(TenantStatus.ACTIVE)
                    .build());

            YearMonth currentMonth = YearMonth.now();
            MonthlyInvoice invoice = monthlyInvoiceRepository.save(MonthlyInvoice.builder()
                    .tenant(tenant)
                    .billingMonth(currentMonth.getMonthValue())
                    .billingYear(currentMonth.getYear())
                    .baseRent(tenant.getMonthlyRentRate())
                    .totalAmountDue(tenant.getMonthlyRentRate())
                    .amountPaid(BigDecimal.ZERO)
                    .dueDate(LocalDate.now().plusDays(3))
                    .paymentStatus(PaymentStatus.PENDING)
                    .build());

            systemReminderRepository.save(SystemReminder.builder()
                    .tenant(tenant)
                    .invoice(invoice)
                    .daysUntilDue(3)
                    .active(true)
                    .build());

            guestConcernRepository.save(GuestConcern.builder()
                    .tenant(tenant)
                    .category(ConcernCategory.WIFI)
                    .encryptedDescription(fieldEncryptionService.encrypt(
                            "Wi-Fi on the second floor drops every evening around 8 PM."))
                    .status(ConcernStatus.OPEN)
                    .build());

            User vacatedUser = userRepository.save(User.builder()
                    .email("alumni@securepg.local")
                    .passwordHash(passwordEncoder.encode("Alumni@Secure123"))
                    .role(Role.GUEST)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build());

            tenantProfileRepository.save(TenantProfile.builder()
                    .user(vacatedUser)
                    .room(room101)
                    .encryptedName(fieldEncryptionService.encrypt("Meera Iyer"))
                    .encryptedPhone(fieldEncryptionService.encrypt("+919812345678"))
                    .encryptedIdProof(fieldEncryptionService.encrypt("XXXX-XXXX-5678"))
                    .checkInDate(LocalDate.now().minusMonths(10))
                    .checkOutDate(LocalDate.now().minusMonths(1))
                    .monthlyRentRate(new BigDecimal("11000.00"))
                    .status(TenantStatus.INACTIVE)
                    .build());
        };
    }
}
