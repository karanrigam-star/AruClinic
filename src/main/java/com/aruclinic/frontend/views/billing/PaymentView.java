package com.aruclinic.frontend.views.billing;

import com.aruclinic.dto.BillDto;
import com.aruclinic.service.BillingService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payment View for processing invoice payments.
 */
@PageTitle("Payment | AruClinic")
@Route("patient/billing/pay")
@CssImport("./themes/aruclinic/billing.css")
public class PaymentView extends VerticalLayout {

    private final BillingService billingService;
    private BillDto invoice;

    private final TextField cardNumber = new TextField("Card Number");
    private final TextField cardName = new TextField("Name on Card");
    private final ComboBox<String> expiryMonth = new ComboBox<>("Expiry Month");
    private final ComboBox<String> expiryYear = new ComboBox<>("Expiry Year");
    private final TextField cvv = new TextField("CVV");
    private final TextField amount = new TextField("Amount");

    private final Button payButton = new Button("Pay Now");
    private final Button cancelButton = new Button("Cancel");

    public PaymentView(BillingService billingService) {
        this.billingService = billingService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Load invoice data
        loadInvoiceData();

        configureComponents();
        add(createPaymentContent());
    }

    private void loadInvoiceData() {
        // In a real app, this would fetch the invoice from the service based on the route parameter
        invoice = new BillDto();
        invoice.setInvoiceId("INV-003");
        invoice.setInvoiceDate(LocalDate.of(2025, 5, 20));
        invoice.setDescription("Follow-up Visit");
        invoice.setAmount(new BigDecimal("100.00"));
        invoice.setStatus("PENDING");
    }

    private void configureComponents() {
        // Card number
        cardNumber.setPlaceholder("1234 5678 9012 3456");
        cardNumber.setRequired(true);
        cardNumber.setRequiredIndicatorVisible(true);
        cardNumber.setWidthFull();
        cardNumber.setPattern("\\d{16}");

        // Card name
        cardName.setPlaceholder("John Doe");
        cardName.setRequired(true);
        cardName.setRequiredIndicatorVisible(true);
        cardName.setWidthFull();

        // Expiry month
        expiryMonth.setItems("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");
        expiryMonth.setRequired(true);
        expiryMonth.setRequiredIndicatorVisible(true);
        expiryMonth.setWidthFull();

        // Expiry year
        expiryYear.setItems("2025", "2026", "2027", "2028", "2029", "2030");
        expiryYear.setRequired(true);
        expiryYear.setRequiredIndicatorVisible(true);
        expiryYear.setWidthFull();
        expiryYear.setValue("2025");

        // CVV
        cvv.setPlaceholder("123");
        cvv.setRequired(true);
        cvv.setRequiredIndicatorVisible(true);
        cvv.setWidthFull();
        cvv.setPattern("\\d{3,4}");

        // Amount
        amount.setValue("₹" + invoice.getAmount().toString());
        amount.setRequired(true);
        amount.setRequiredIndicatorVisible(true);
        amount.setWidthFull();
        amount.setReadOnly(true);

        // Pay button
        payButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        payButton.addClassName("aruclinic-payment-btn");
        payButton.addClassName("primary");
        payButton.addClickListener(e -> handlePayment());

        // Cancel button
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClassName("aruclinic-payment-btn");
        cancelButton.addClassName("secondary");
        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("patient/billing")));
    }

    private Component createPaymentContent() {
        Div content = new Div();
        content.addClassName("aruclinic-payment-form");
        content.setWidthFull();
        content.setMaxWidth("600px");
        content.getStyle().set("margin", "0 auto");

        // Card
        Div card = new Div();
        card.addClassName("aruclinic-payment-card");

        // Header
        Div header = new Div();
        header.addClassName("aruclinic-payment-header");

        H1 title = new H1("Payment");
        title.addClassName("aruclinic-payment-title");

        Span amountSpan = new Span("₹" + invoice.getAmount().toString());
        amountSpan.addClassName("aruclinic-payment-amount");

        header.add(title, amountSpan);

        // Invoice info
        Div invoiceInfo = new Div();
        invoiceInfo.addClassName("aruclinic-payment-invoice-info");

        Paragraph invoiceText = new Paragraph("Invoice #" + invoice.getInvoiceId() + " - " + invoice.getDescription());
        invoiceText.addClassName("aruclinic-payment-invoice-text");

        invoiceInfo.add(invoiceText);

        // Form fields
        Div form = new Div();
        form.addClassName("aruclinic-payment-form-fields");

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();

        formLayout.add(cardNumber);
        formLayout.add(cardName);

        // Expiry row
        HorizontalLayout expiryRow = new HorizontalLayout();
        expiryRow.setWidthFull();
        expiryRow.add(expiryMonth, expiryYear);

        formLayout.add(expiryRow);
        formLayout.add(cvv);
        formLayout.add(amount);

        form.add(formLayout);

        // Payment methods
        Div paymentMethods = new Div();
        paymentMethods.addClassName("aruclinic-payment-methods");

        H2 methodsTitle = new H2("Payment Method");
        methodsTitle.addClassName("aruclinic-payment-methods-title");

        Div methodList = new Div();
        methodList.addClassName("aruclinic-payment-method-list");

        // Credit Card method (selected by default)
        Div creditCardMethod = new Div();
        creditCardMethod.addClassName("aruclinic-payment-method");
        creditCardMethod.addClassName("selected");

        Div creditCardIcon = new Div();
        creditCardIcon.addClassName("aruclinic-payment-method-icon");
        creditCardIcon.add(new Icon(VaadinIcon.CREDIT_CARD));

        Div creditCardInfo = new Div();
        creditCardInfo.addClassName("aruclinic-payment-method-info");

        Span creditCardName = new Span("Credit Card");
        creditCardName.addClassName("aruclinic-payment-method-name");

        Span creditCardDesc = new Span("Pay with your credit or debit card");
        creditCardDesc.addClassName("aruclinic-payment-method-description");

        Div creditCardRadio = new Div();
        creditCardRadio.addClassName("aruclinic-payment-method-radio");

        creditCardInfo.add(creditCardName, creditCardDesc);
        creditCardMethod.add(creditCardIcon, creditCardInfo, creditCardRadio);

        // PayPal method
        Div paypalMethod = new Div();
        paypalMethod.addClassName("aruclinic-payment-method");

        Div paypalIcon = new Div();
        paypalIcon.addClassName("aruclinic-payment-method-icon");
        paypalIcon.add(new Icon(VaadinIcon.GLOBE));

        Div paypalInfo = new Div();
        paypalInfo.addClassName("aruclinic-payment-method-info");

        Span paypalName = new Span("PayPal");
        paypalName.addClassName("aruclinic-payment-method-name");

        Span paypalDesc = new Span("Pay with your PayPal account");
        paypalDesc.addClassName("aruclinic-payment-method-description");

        Div paypalRadio = new Div();
        paypalRadio.addClassName("aruclinic-payment-method-radio");

        paypalInfo.add(paypalName, paypalDesc);
        paypalMethod.add(paypalIcon, paypalInfo, paypalRadio);

        // Bank Transfer method
        Div bankMethod = new Div();
        bankMethod.addClassName("aruclinic-payment-method");

        Div bankIcon = new Div();
        bankIcon.addClassName("aruclinic-payment-method-icon");
        bankIcon.add(new Icon(VaadinIcon.BUILDING));

        Div bankInfo = new Div();
        bankInfo.addClassName("aruclinic-payment-method-info");

        Span bankName = new Span("Bank Transfer");
        bankName.addClassName("aruclinic-payment-method-name");

        Span bankDesc = new Span("Transfer directly from your bank account");
        bankDesc.addClassName("aruclinic-payment-method-description");

        Div bankRadio = new Div();
        bankRadio.addClassName("aruclinic-payment-method-radio");

        bankInfo.add(bankName, bankDesc);
        bankMethod.add(bankIcon, bankInfo, bankRadio);

        methodList.add(creditCardMethod, paypalMethod, bankMethod);
        paymentMethods.add(methodsTitle, methodList);

        // Actions
        Div actions = new Div();
        actions.addClassName("aruclinic-payment-actions");
        actions.add(cancelButton, payButton);

        card.add(header, invoiceInfo, form, paymentMethods, actions);
        content.add(card);

        return content;
    }

    private void handlePayment() {
        // Validate all fields
        if (!validateFields()) {
            return;
        }

        // Process payment
        try {
            // In a real app, this would call the billing service to process the payment
            billingService.processPayment(invoice.getInvoiceId(), invoice.getAmount());

            Notification.show(
                "Payment of ₹" + invoice.getAmount().toString() + " processed successfully!",
                5000,
                Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Navigate to payment success page
            getUI().ifPresent(ui -> ui.navigate("patient/billing/payment-success"));

        } catch (Exception e) {
            showError("Payment failed: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (cardNumber.getValue().trim().isEmpty()) {
            cardNumber.setErrorMessage("Card number is required");
            cardNumber.setInvalid(true);
            isValid = false;
        } else if (!cardNumber.getValue().trim().matches("\\d{16}")) {
            cardNumber.setErrorMessage("Card number must be 16 digits");
            cardNumber.setInvalid(true);
            isValid = false;
        } else {
            cardNumber.setErrorMessage(null);
            cardNumber.setInvalid(false);
        }

        if (cardName.getValue().trim().isEmpty()) {
            cardName.setErrorMessage("Name on card is required");
            cardName.setInvalid(true);
            isValid = false;
        } else {
            cardName.setErrorMessage(null);
            cardName.setInvalid(false);
        }

        if (expiryMonth.getValue() == null) {
            expiryMonth.setErrorMessage("Expiry month is required");
            expiryMonth.setInvalid(true);
            isValid = false;
        } else {
            expiryMonth.setErrorMessage(null);
            expiryMonth.setInvalid(false);
        }

        if (expiryYear.getValue() == null) {
            expiryYear.setErrorMessage("Expiry year is required");
            expiryYear.setInvalid(true);
            isValid = false;
        } else {
            expiryYear.setErrorMessage(null);
            expiryYear.setInvalid(false);
        }

        if (cvv.getValue().trim().isEmpty()) {
            cvv.setErrorMessage("CVV is required");
            cvv.setInvalid(true);
            isValid = false;
        } else if (!cvv.getValue().trim().matches("\\d{3,4}")) {
            cvv.setErrorMessage("CVV must be 3 or 4 digits");
            cvv.setInvalid(true);
            isValid = false;
        } else {
            cvv.setErrorMessage(null);
            cvv.setInvalid(false);
        }

        return isValid;
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
