# PAYMENT INTEGRATION GUIDE

## Stripe Setup

### 1. Create Stripe Account
- Go to https://stripe.com
- Sign up for a developer account
- Get your API keys from Dashboard > Developers > API keys

### 2. Configure Stripe API Key
Update `application.properties`:
```properties
stripe.api.key=sk_test_YOUR_STRIPE_SECRET_KEY
```

### 3. Frontend Integration

#### Install Stripe.js
```bash
npm install @stripe/react-stripe-js @stripe/stripe-js
```

#### Payment Flow Example (React)

```javascript
import { useState } from 'react';
import { loadStripe } from '@stripe/stripe-js';
import { CardElement, Elements, useStripe, useElements } from '@stripe/react-stripe-js';

const stripePromise = loadStripe('pk_test_YOUR_STRIPE_PUBLIC_KEY');

function CheckoutForm({ orderId }) {
  const stripe = useStripe();
  const elements = useElements();
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    // Step 1: Create Payment Intent
    const intentResponse = await fetch('/api/payment/create-intent', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ orderId })
    });
    const intentData = await intentResponse.json();
    const clientSecret = intentData.clientSecret;

    // Step 2: Confirm Payment with Stripe.js
    const { error, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
      payment_method: {
        card: elements.getElement(CardElement)
      }
    });

    if (error) {
      console.error('Payment failed:', error);
    } else {
      // Step 3: Confirm Payment on Backend
      const confirmResponse = await fetch('/api/payment/confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          orderId,
          paymentIntentId: paymentIntent.id
        })
      });
      const confirmData = await confirmResponse.json();
      console.log('Payment confirmed:', confirmData);
    }
    setLoading(false);
  };

  return (
    <form onSubmit={handleSubmit}>
      <CardElement />
      <button disabled={!stripe || loading}>Pay Now</button>
    </form>
  );
}

export default function Checkout({ orderId }) {
  return (
    <Elements stripe={stripePromise}>
      <CheckoutForm orderId={orderId} />
    </Elements>
  );
}
```

### 4. Payment Flow

1. **Create Payment Intent**
   ```
   POST /api/payment/create-intent
   Body: { "orderId": 1 }
   ```

2. **Confirm Payment** (After Stripe processes payment)
   ```
   POST /api/payment/confirm
   Body: {
     "orderId": 1,
     "paymentIntentId": "pi_xxxxxxxxxxxx"
   }
   ```

3. **Check Payment Status**
   ```
   GET /api/payment/status/{orderId}
   ```

### 5. Testing with Stripe Test Cards

- **Success**: 4242 4242 4242 4242
- **Decline**: 4000 0000 0000 0002
- **Auth Required**: 4000 0025 0000 3155
- **Expiry**: Any future date
- **CVC**: Any 3 digits

### 6. Webhooks (Optional but Recommended)

For production, implement webhooks to handle async payment events:

```java
@PostMapping("/webhook")
public ResponseEntity<?> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
    // Verify webhook signature
    // Handle payment events (charge.succeeded, charge.failed, etc.)
    return new ResponseEntity<>(HttpStatus.OK);
}
```

## API Endpoints

```
POST   /api/payment/create-intent      - Create payment intent
POST   /api/payment/confirm             - Confirm payment
GET    /api/payment/status/{orderId}    - Get payment status
GET    /api/payment/order/{orderId}     - Get payment by order
GET    /api/payment/{id}                - Get payment details
POST   /api/payment/refund              - Process refund (Admin)
```

## Error Handling

Common error scenarios:
- Insufficient funds
- Card expired
- Incorrect CVC
- Address mismatch
- Rate limiting

All errors return appropriate HTTP status codes and error messages.
