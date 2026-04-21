-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 21, 2026 at 12:48 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `database`
--

-- --------------------------------------------------------

--
-- Table structure for table `addresses`
--

CREATE TABLE `addresses` (
  `id_address` bigint(20) NOT NULL,
  `address_line1` varchar(255) NOT NULL,
  `address_line2` varchar(255) DEFAULT NULL,
  `city` varchar(255) NOT NULL,
  `country` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `full_name` varchar(255) NOT NULL,
  `is_default` bit(1) NOT NULL,
  `phone_number` varchar(255) NOT NULL,
  `postal_code` varchar(255) NOT NULL,
  `state` varchar(255) DEFAULT NULL,
  `type` enum('BILLING','BOTH','SHIPPING') DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `carts`
--

CREATE TABLE `carts` (
  `id_cart` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `cart_items`
--

CREATE TABLE `cart_items` (
  `id_cart_item` bigint(20) NOT NULL,
  `added_at` datetime(6) DEFAULT NULL,
  `price_at_add` decimal(10,2) NOT NULL,
  `quantity` int(11) NOT NULL,
  `cart_id` bigint(20) NOT NULL,
  `variant_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `id_category` bigint(20) NOT NULL,
  `description` text DEFAULT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`id_category`, `description`, `name`) VALUES
(1, 'home and decor', 'home'),
(2, 'electronics and devices', 'electronics'),
(3, 'agriculture and garden', 'agriculture'),
(4, 'modern clothing items', 'clothing'),
(5, 'sports and gym equipements', 'sport');

-- --------------------------------------------------------

--
-- Table structure for table `chatbot_messages`
--

CREATE TABLE `chatbot_messages` (
  `id_msg` bigint(20) NOT NULL,
  `answer` text DEFAULT NULL,
  `date_msg` datetime(6) DEFAULT NULL,
  `question` text NOT NULL,
  `user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `clients`
--

CREATE TABLE `clients` (
  `id_client` bigint(20) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `clients`
--

INSERT INTO `clients` (`id_client`, `address`, `country`, `name`, `phone_number`, `user_id`) VALUES
(2, 'zayneb zouaoui, tunisia nabeul hammam al ghazez, hammam al ghazez, 8025, Tunisia', 'TN', 'client', '+21622489141', 2),
(3, NULL, NULL, 'client1', '+21647896321', 3),
(4, 'zayneb zouaoui, tunisia nabeul hammam al ghazez, hammam al ghazez, 8025, Tunisia', 'TN', 'client2', '24093084', 4);

-- --------------------------------------------------------

--
-- Table structure for table `invoices`
--

CREATE TABLE `invoices` (
  `id_invoice` bigint(20) NOT NULL,
  `billing_address` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `currency` varchar(10) NOT NULL,
  `discount_amount` decimal(10,2) DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `internal_notes` varchar(1000) DEFAULT NULL,
  `invoice_number` varchar(50) NOT NULL,
  `issue_date` date NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `paid_date` date DEFAULT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `payment_reference` varchar(100) DEFAULT NULL,
  `pdf_generated` bit(1) DEFAULT NULL,
  `pdf_path` varchar(500) DEFAULT NULL,
  `shipping_address` varchar(500) DEFAULT NULL,
  `shipping_cost` decimal(10,2) DEFAULT NULL,
  `status` enum('CANCELLED','DRAFT','ISSUED','OVERDUE','PAID','PARTIALLY_PAID','REFUNDED','SENT') NOT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `tax_amount` decimal(12,2) NOT NULL,
  `tax_rate` decimal(5,2) DEFAULT NULL,
  `tax_type` varchar(50) DEFAULT NULL,
  `total_amount` decimal(12,2) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `order_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id_notif` bigint(20) NOT NULL,
  `date_send` date DEFAULT NULL,
  `message` text DEFAULT NULL,
  `status` enum('READ','UNREAD') DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `id_order` bigint(20) NOT NULL,
  `date_order` date DEFAULT NULL,
  `status` enum('CANCELLED','CONFIRMED','DELIVERED','PAID','PENDING','PROCESSING','REFUNDED','SHIPPED') DEFAULT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`id_order`, `date_order`, `status`, `total_amount`, `client_id`, `user_id`) VALUES
(2, '2026-04-17', 'PENDING', 163.50, 2, 2),
(3, '2026-04-18', 'PENDING', 30.51, 2, 2),
(4, '2026-04-18', 'PENDING', 163.50, 4, 4);

-- --------------------------------------------------------

--
-- Table structure for table `order_details`
--

CREATE TABLE `order_details` (
  `id_detail` bigint(20) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `quantity` int(11) NOT NULL,
  `order_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `variant_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `order_details`
--

INSERT INTO `order_details` (`id_detail`, `price`, `quantity`, `order_id`, `product_id`, `variant_id`) VALUES
(1, 150.00, 1, 2, 9, 9),
(2, 20.00, 1, 3, 2, 2),
(3, 150.00, 1, 4, 9, 9);

-- --------------------------------------------------------

--
-- Table structure for table `payments`
--

CREATE TABLE `payments` (
  `id_payment` bigint(20) NOT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `date_payment` date DEFAULT NULL,
  `failure_reason` varchar(255) DEFAULT NULL,
  `mode_payment` varchar(255) DEFAULT NULL,
  `paid_at` datetime(6) DEFAULT NULL,
  `payment_intent_id` varchar(255) DEFAULT NULL,
  `payment_method` enum('BANK_TRANSFER','CASH_ON_DELIVERY','E_DINAR','KONNECT','PAYMEE','PAYPAL','SMT_MONETIQUE','STRIPE','TWO_CHECKOUT') DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','FAILED','PENDING','PROCESSING','REFUNDED') DEFAULT NULL,
  `transaction_id` varchar(255) DEFAULT NULL,
  `order_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payments`
--

INSERT INTO `payments` (`id_payment`, `amount`, `currency`, `date_payment`, `failure_reason`, `mode_payment`, `paid_at`, `payment_intent_id`, `payment_method`, `status`, `transaction_id`, `order_id`) VALUES
(1, 163.50, NULL, '2026-04-17', NULL, 'D17', NULL, NULL, NULL, 'PENDING', NULL, 2),
(2, 30.51, NULL, '2026-04-18', NULL, 'D17', NULL, NULL, NULL, 'PENDING', NULL, 3),
(3, 163.50, NULL, '2026-04-18', NULL, 'D17', NULL, NULL, NULL, 'PENDING', NULL, 4);

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `id_product` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `rating` decimal(3,2) DEFAULT 4.50,
  `updated_at` datetime(6) DEFAULT NULL,
  `category_id` bigint(20) DEFAULT NULL,
  `stock` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`id_product`, `created_at`, `description`, `name`, `price`, `rating`, `updated_at`, `category_id`, `stock`) VALUES
(2, '2026-04-17 14:31:28.000000', 'adidas blue cap', 'adidas cap', 20.00, NULL, '2026-04-17 14:31:28.000000', 4, NULL),
(7, '2026-04-17 16:43:43.000000', 'brown leather jacket', 'leather jacket', 150.00, NULL, '2026-04-17 16:43:43.000000', 4, 50),
(8, '2026-04-17 16:46:29.000000', 'white wireless mouse', 'wireless mouse', 20.00, NULL, '2026-04-17 16:46:29.000000', 2, 30),
(9, '2026-04-17 16:49:26.000000', 'white couch', 'couch', 150.00, NULL, '2026-04-17 16:49:26.000000', 1, 20),
(10, '2026-04-17 16:50:31.000000', 'black wireless airpods', 'airpods', 46.00, NULL, '2026-04-17 16:50:31.000000', 2, 10);

-- --------------------------------------------------------

--
-- Table structure for table `product_images`
--

CREATE TABLE `product_images` (
  `id_image` bigint(20) NOT NULL,
  `alt_text` varchar(255) DEFAULT NULL,
  `created_at` date DEFAULT NULL,
  `display_order` int(11) NOT NULL,
  `image_url` varchar(255) NOT NULL,
  `is_primary` bit(1) NOT NULL,
  `product_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `product_images`
--

INSERT INTO `product_images` (`id_image`, `alt_text`, `created_at`, `display_order`, `image_url`, `is_primary`, `product_id`) VALUES
(2, NULL, '2026-04-17', 0, 'https://i.imgur.com/XghVSH9.png', b'1', 2),
(7, NULL, '2026-04-17', 0, 'https://i.imgur.com/O8yRgPO.png', b'1', 7),
(8, NULL, '2026-04-17', 0, 'https://i.imgur.com/1j5gT7O_d.png?maxwidth=520&shape=thumb&fidelity=high', b'1', 8),
(9, NULL, '2026-04-17', 0, 'https://i.imgur.com/BtiZmIA_d.png?maxwidth=520&shape=thumb&fidelity=high', b'1', 9),
(10, NULL, '2026-04-17', 0, 'https://i.imgur.com/U3bL1tK_d.png?maxwidth=520&shape=thumb&fidelity=high', b'1', 10);

-- --------------------------------------------------------

--
-- Table structure for table `product_variants`
--

CREATE TABLE `product_variants` (
  `id_variant` bigint(20) NOT NULL,
  `additional_price` decimal(10,2) DEFAULT NULL,
  `color` varchar(255) DEFAULT NULL,
  `size` varchar(255) DEFAULT NULL,
  `sku` varchar(255) NOT NULL,
  `stock_quantity` int(11) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `product_variants`
--

INSERT INTO `product_variants` (`id_variant`, `additional_price`, `color`, `size`, `sku`, `stock_quantity`, `product_id`, `image_url`) VALUES
(2, 0.00, 'white', 'M', 'PROD-1776432490819', 14, 2, 'https://i.imgur.com/orOUlmP.png'),
(7, 0.00, 'black', 'L', 'PROD-1776440564109', 50, 7, 'https://i.imgur.com/jkU4Yhi.png'),
(8, 0.00, '', '', 'PROD-1776440719758', 0, 8, ''),
(9, 0.00, '', '', 'PROD-1776440931417', 0, 9, ''),
(10, 0.00, '', '', 'PROD-1776440979012', 0, 10, '');

-- --------------------------------------------------------

--
-- Table structure for table `purchase_orders`
--

CREATE TABLE `purchase_orders` (
  `id_purchase_order` bigint(20) NOT NULL,
  `actual_delivery_date` date DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `currency` varchar(10) DEFAULT NULL,
  `discount_amount` decimal(12,2) DEFAULT NULL,
  `expected_delivery_date` date DEFAULT NULL,
  `internal_notes` varchar(1000) DEFAULT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `order_date` date NOT NULL,
  `order_number` varchar(50) NOT NULL,
  `paid_date` date DEFAULT NULL,
  `payment_due_date` date DEFAULT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `payment_reference` varchar(100) DEFAULT NULL,
  `shipping_cost` decimal(12,2) DEFAULT NULL,
  `shipping_method` varchar(50) DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','CONFIRMED','DRAFT','PENDING','PROCESSING','RECEIVED','SHIPPED') NOT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `tax_amount` decimal(12,2) DEFAULT NULL,
  `total_amount` decimal(12,2) NOT NULL,
  `tracking_number` varchar(100) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `warehouse_location` varchar(200) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `supplier_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `purchase_order_items`
--

CREATE TABLE `purchase_order_items` (
  `id_item` bigint(20) NOT NULL,
  `damaged_quantity` int(11) DEFAULT NULL,
  `line_total` decimal(12,2) NOT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `received_quantity` int(11) DEFAULT NULL,
  `unit_cost` decimal(10,2) NOT NULL,
  `purchase_order_id` bigint(20) NOT NULL,
  `variant_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `shipments`
--

CREATE TABLE `shipments` (
  `id_ship` bigint(20) NOT NULL,
  `carrier` varchar(255) DEFAULT NULL,
  `date_ship` date DEFAULT NULL,
  `delivered_at` datetime(6) DEFAULT NULL,
  `estimated_delivery_date` date DEFAULT NULL,
  `shipping_cost` decimal(10,2) DEFAULT NULL,
  `shipping_method` enum('EXPRESS','INTERNATIONAL','OVERNIGHT','STANDARD') DEFAULT NULL,
  `status` enum('CANCELLED','CONFIRMED','DELIVERED','PAID','PENDING','PROCESSING','REFUNDED','SHIPPED') DEFAULT NULL,
  `tracking_number` varchar(255) DEFAULT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  `shipping_address_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `stock`
--

CREATE TABLE `stock` (
  `id_stock` bigint(20) NOT NULL,
  `quantity` int(11) NOT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `variant_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `stock`
--

INSERT INTO `stock` (`id_stock`, `quantity`, `product_id`, `variant_id`) VALUES
(1, 14, 2, 2),
(14, 50, 7, NULL),
(15, 50, NULL, 7),
(17, 17, NULL, 8),
(18, 20, 9, NULL),
(19, 29, NULL, 9),
(20, 10, 10, NULL),
(21, 24, NULL, 10);

-- --------------------------------------------------------

--
-- Table structure for table `stock_movements`
--

CREATE TABLE `stock_movements` (
  `id_movement` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `quantity` int(11) NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `reference_id` bigint(20) DEFAULT NULL,
  `reference_type` varchar(255) DEFAULT NULL,
  `stock_after` int(11) NOT NULL,
  `stock_before` int(11) NOT NULL,
  `type` enum('ADJUSTMENT','CANCELLATION','DAMAGE','INITIAL_STOCK','LOSS','PURCHASE','RETURN','SALE','TRANSFER') NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `variant_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `suppliers`
--

CREATE TABLE `suppliers` (
  `id_supplier` bigint(20) NOT NULL,
  `address_line1` varchar(200) DEFAULT NULL,
  `address_line2` varchar(200) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `code` varchar(50) DEFAULT NULL,
  `contact_person` varchar(100) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `delivery_rating` decimal(3,2) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `first_order_date` date DEFAULT NULL,
  `last_order_date` date DEFAULT NULL,
  `late_deliveries` int(11) DEFAULT NULL,
  `lead_time` int(11) DEFAULT NULL,
  `minimum_order` decimal(10,2) DEFAULT NULL,
  `mobile` varchar(20) DEFAULT NULL,
  `name` varchar(200) NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `on_time_deliveries` int(11) DEFAULT NULL,
  `payment_terms` int(11) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `postal_code` varchar(20) DEFAULT NULL,
  `quality_rating` decimal(3,2) DEFAULT NULL,
  `rating` decimal(3,2) DEFAULT NULL,
  `service_rating` decimal(3,2) DEFAULT NULL,
  `state` varchar(100) DEFAULT NULL,
  `status` enum('ACTIVE','BLACKLISTED','INACTIVE','SUSPENDED') NOT NULL,
  `tax_id` varchar(50) DEFAULT NULL,
  `total_orders` int(11) DEFAULT NULL,
  `total_spent` decimal(12,2) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `website` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `suppliers`
--

INSERT INTO `suppliers` (`id_supplier`, `address_line1`, `address_line2`, `city`, `code`, `contact_person`, `country`, `created_at`, `delivery_rating`, `description`, `email`, `first_order_date`, `last_order_date`, `late_deliveries`, `lead_time`, `minimum_order`, `mobile`, `name`, `notes`, `on_time_deliveries`, `payment_terms`, `phone`, `postal_code`, `quality_rating`, `rating`, `service_rating`, `state`, `status`, `tax_id`, `total_orders`, `total_spent`, `updated_at`, `website`) VALUES
(1, 'Zone Industrielle', 'Bloc A', 'Tunis', 'SUP001', 'Ahmed Ben Ali', 'Tunisia', '2026-04-17 21:52:05.000000', 4.50, 'Fournisseur de tissus et vêtements', 'contact@globaltextiles.com', '2024-01-10', '2026-03-15', 5, 7, 100.00, '20111222', 'Global Textiles', 'Partenaire fiable', 120, 0, '71222333', '1000', 4.70, 4.60, 4.60, 'Tunis', 'ACTIVE', 'TN123456', 150, 50000.00, '2026-04-17 21:52:05.000000', 'www.globaltextiles.com'),
(2, 'Rue du Cuir', NULL, 'Sfax', 'SUP002', 'Sami Trabelsi', 'Tunisia', '2026-04-17 21:52:05.000000', 4.20, 'Spécialisé en cuir et accessoires', 'sales@leatherpro.com', '2024-02-05', '2026-02-20', 10, 10, 50.00, '22123456', 'Leather Pro', NULL, 90, 0, '73444555', '3000', 4.50, 4.30, 4.30, 'Sfax', 'ACTIVE', 'TN654321', 100, 30000.00, '2026-04-17 21:52:05.000000', 'www.leatherpro.com'),
(3, 'Tech Park', 'Bureau 12', 'Ariana', 'SUP003', 'Nour Haddad', 'Tunisia', '2026-04-17 21:52:05.000000', 3.80, 'Fournisseur équipements électroniques', 'info@electrosupply.com', '2023-11-01', '2026-01-30', 20, 14, 200.00, '55112233', 'Electro Supply', 'Livraisons parfois lentes', 70, 0, '70199887', '2080', 4.00, 3.90, 3.90, 'Ariana', 'INACTIVE', 'TN789456', 80, 45000.00, '2026-04-17 21:52:05.000000', 'www.electrosupply.com');

-- --------------------------------------------------------

--
-- Table structure for table `transporteurs`
--

CREATE TABLE `transporteurs` (
  `id_transporteur` bigint(20) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `delivery_fee` decimal(38,2) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `transporteurs`
--

INSERT INTO `transporteurs` (`id_transporteur`, `address`, `created_at`, `delivery_fee`, `email`, `name`, `phone`, `status`) VALUES
(1, 'Tunis Centre, Tunisia', '2026-04-17 21:50:27.000000', 8.50, 'contact@dhl.com', 'DHL Express', '70123456', 'active'),
(2, 'Ariana, Tunisia', '2026-04-17 21:50:27.000000', 7.00, 'info@aramex.com', 'Aramex', '71111222', 'active'),
(3, 'La Poste Tunisienne, Tunis', '2026-04-17 21:50:27.000000', 5.00, 'contact@poste.tn', 'Rapid Poste', '1828', 'active'),
(4, 'Sfax, Tunisia', '2026-04-17 21:50:27.000000', 6.50, 'support@colisprive.tn', 'Colis Privé', '74222333', 'inactive'),
(5, 'Sousse, Tunisia', '2026-04-17 21:50:27.000000', 9.00, 'express@delivery.tn', 'Express Delivery', '73333444', 'active');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id_user` bigint(20) NOT NULL,
  `created_at` date DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `email_verification_token` varchar(255) DEFAULT NULL,
  `email_verification_token_expiry` datetime(6) DEFAULT NULL,
  `email_verified` bit(1) DEFAULT NULL,
  `fullname` varchar(255) NOT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `password_reset_token` varchar(255) DEFAULT NULL,
  `password_reset_token_expiry` datetime(6) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `role` enum('ADMIN','CLIENT') NOT NULL,
  `username` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id_user`, `created_at`, `email`, `email_verification_token`, `email_verification_token_expiry`, `email_verified`, `fullname`, `is_active`, `password`, `password_reset_token`, `password_reset_token_expiry`, `phone_number`, `role`, `username`) VALUES
(1, '2026-04-17', 'admin@gmail.com', '08860991-0d93-49a5-94f7-37c5ddee57b6', '2026-04-18 12:37:25.000000', b'0', 'admin', b'1', '$2a$10$..0Vtja5rK8KtD62ogp4gezIm.ysk/AeYTgRSVCIP/Jtb1ifNj7Zm', NULL, NULL, '+21624093084', 'ADMIN', 'admin@gmail.com'),
(2, '2026-04-17', 'client@gmail.com', '330d05d4-ddc9-48e0-ac0a-0a1df1708aaa', '2026-04-18 12:38:21.000000', b'0', 'client', b'1', '$2a$10$3ElLUGh914N9IqpD8zHmFusvhkGu9uCzI1IBTjtgosOigjLyYNJbC', NULL, NULL, '+21622489141', 'CLIENT', 'client@gmail.com'),
(3, '2026-04-17', 'client1@gmail.com', '9620c37a-da73-4232-8bc2-51405bebab5d', '2026-04-18 12:39:12.000000', b'0', 'client1', b'1', '$2a$10$MO7qnYsSF1e8zEBu07.3Ru9XBgQw5sCZTcFKJPlyURmRLYr6LezFC', NULL, NULL, '+21647896321', 'CLIENT', 'client1@gmail.com'),
(4, '2026-04-18', 'client2@gmail.com', 'cb5cc5b1-55cd-4791-b0ba-09701b1649ec', '2026-04-19 16:18:31.000000', b'0', 'client2', b'1', '$2a$10$nP1LjbnfHWrFLxOTyrcuF.v/gPfikCkOj1oYu2.iwfUoNipZ2v6M.', NULL, NULL, '24093084', 'CLIENT', 'client2@gmail.com');

-- --------------------------------------------------------

--
-- Table structure for table `wishlists`
--

CREATE TABLE `wishlists` (
  `id_wishlist` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `wishlist_items`
--

CREATE TABLE `wishlist_items` (
  `id_wishlist_item` bigint(20) NOT NULL,
  `added_at` datetime(6) DEFAULT NULL,
  `variant_id` bigint(20) NOT NULL,
  `wishlist_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `addresses`
--
ALTER TABLE `addresses`
  ADD PRIMARY KEY (`id_address`),
  ADD KEY `FK1fa36y2oqhao3wgg2rw1pi459` (`user_id`);

--
-- Indexes for table `carts`
--
ALTER TABLE `carts`
  ADD PRIMARY KEY (`id_cart`),
  ADD UNIQUE KEY `UK64t7ox312pqal3p7fg9o503c2` (`user_id`);

--
-- Indexes for table `cart_items`
--
ALTER TABLE `cart_items`
  ADD PRIMARY KEY (`id_cart_item`),
  ADD KEY `FKpcttvuq4mxppo8sxggjtn5i2c` (`cart_id`),
  ADD KEY `FK5yyw1o0dor9gmxfra1dqvn4qa` (`variant_id`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id_category`),
  ADD UNIQUE KEY `UKt8o6pivur7nn124jehx7cygw5` (`name`);

--
-- Indexes for table `chatbot_messages`
--
ALTER TABLE `chatbot_messages`
  ADD PRIMARY KEY (`id_msg`),
  ADD KEY `FK7b4xj26r6rgyoq77u8vd4gskr` (`user_id`);

--
-- Indexes for table `clients`
--
ALTER TABLE `clients`
  ADD PRIMARY KEY (`id_client`),
  ADD UNIQUE KEY `UKsmrp6gi0tckq1w5rnd7boyowu` (`user_id`);

--
-- Indexes for table `invoices`
--
ALTER TABLE `invoices`
  ADD PRIMARY KEY (`id_invoice`),
  ADD UNIQUE KEY `UKl1x55mfsay7co0r3m9ynvipd5` (`invoice_number`),
  ADD UNIQUE KEY `UKe718q5klx5pempy28p2nx88a6` (`order_id`),
  ADD KEY `FKbwr4d4vyqf2bkoetxtt8j9dx7` (`user_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id_notif`),
  ADD KEY `FK9y21adhxn0ayjhfocscqox7bh` (`user_id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`id_order`),
  ADD KEY `FKm2dep9derpoaehshbkkatam3v` (`client_id`),
  ADD KEY `FK32ql8ubntj5uh44ph9659tiih` (`user_id`);

--
-- Indexes for table `order_details`
--
ALTER TABLE `order_details`
  ADD PRIMARY KEY (`id_detail`),
  ADD KEY `FKjyu2qbqt8gnvno9oe9j2s2ldk` (`order_id`),
  ADD KEY `FK4q98utpd73imf4yhttm3w0eax` (`product_id`),
  ADD KEY `FK63mqrfva3vf2gx2sublm4m51w` (`variant_id`);

--
-- Indexes for table `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`id_payment`),
  ADD KEY `FK81gagumt0r8y3rmudcgpbk42l` (`order_id`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id_product`),
  ADD KEY `FKog2rp4qthbtt2lfyhfo32lsw9` (`category_id`);

--
-- Indexes for table `product_images`
--
ALTER TABLE `product_images`
  ADD PRIMARY KEY (`id_image`),
  ADD KEY `FKqnq71xsohugpqwf3c9gxmsuy` (`product_id`);

--
-- Indexes for table `product_variants`
--
ALTER TABLE `product_variants`
  ADD PRIMARY KEY (`id_variant`),
  ADD UNIQUE KEY `UKq935p2d1pbjm39n0063ghnfgn` (`sku`),
  ADD KEY `FKosqitn4s405cynmhb87lkvuau` (`product_id`);

--
-- Indexes for table `purchase_orders`
--
ALTER TABLE `purchase_orders`
  ADD PRIMARY KEY (`id_purchase_order`),
  ADD UNIQUE KEY `UKnqsdqb8p2iobsmeaa2jxxw7k` (`order_number`),
  ADD KEY `FKcgqvocxciov7fq8tev9p50e5d` (`created_by`),
  ADD KEY `FKrpdasmb8y8xs5tiy4369xpinq` (`supplier_id`);

--
-- Indexes for table `purchase_order_items`
--
ALTER TABLE `purchase_order_items`
  ADD PRIMARY KEY (`id_item`),
  ADD KEY `FKo3yj8ocbw2kav38548t22hgh8` (`purchase_order_id`),
  ADD KEY `FK8vvrbcsu89a1ddsrfygegjio` (`variant_id`);

--
-- Indexes for table `shipments`
--
ALTER TABLE `shipments`
  ADD PRIMARY KEY (`id_ship`),
  ADD UNIQUE KEY `UKhrhy2yghr8dampg1jtecuekvp` (`order_id`),
  ADD KEY `FKplhb19s5v79q0s2ra80eyts0f` (`shipping_address_id`);

--
-- Indexes for table `stock`
--
ALTER TABLE `stock`
  ADD PRIMARY KEY (`id_stock`),
  ADD UNIQUE KEY `UKkhabtqwr86p7x9mt2krib98tx` (`product_id`),
  ADD UNIQUE KEY `UKqj1rdyr6b1teqholdc4ttibmb` (`variant_id`);

--
-- Indexes for table `stock_movements`
--
ALTER TABLE `stock_movements`
  ADD PRIMARY KEY (`id_movement`),
  ADD KEY `FKfqq1iu0gt0la6ruk2o62bry5v` (`user_id`),
  ADD KEY `FKo3rdw1xgft64g9fr8d3rnbt1q` (`variant_id`);

--
-- Indexes for table `suppliers`
--
ALTER TABLE `suppliers`
  ADD PRIMARY KEY (`id_supplier`),
  ADD UNIQUE KEY `UK8kh5crh75ye2imfi5yv37p61o` (`code`);

--
-- Indexes for table `transporteurs`
--
ALTER TABLE `transporteurs`
  ADD PRIMARY KEY (`id_transporteur`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id_user`),
  ADD UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  ADD UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`);

--
-- Indexes for table `wishlists`
--
ALTER TABLE `wishlists`
  ADD PRIMARY KEY (`id_wishlist`),
  ADD UNIQUE KEY `UKobh8c909a28dx3aqh4cbdhh25` (`user_id`);

--
-- Indexes for table `wishlist_items`
--
ALTER TABLE `wishlist_items`
  ADD PRIMARY KEY (`id_wishlist_item`),
  ADD KEY `FKd20mh8fp7bij1shxeor4vwwe` (`variant_id`),
  ADD KEY `FKkem9l8vd14pk3cc4elnpl0n00` (`wishlist_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `addresses`
--
ALTER TABLE `addresses`
  MODIFY `id_address` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `carts`
--
ALTER TABLE `carts`
  MODIFY `id_cart` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `cart_items`
--
ALTER TABLE `cart_items`
  MODIFY `id_cart_item` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `id_category` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `chatbot_messages`
--
ALTER TABLE `chatbot_messages`
  MODIFY `id_msg` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `clients`
--
ALTER TABLE `clients`
  MODIFY `id_client` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `invoices`
--
ALTER TABLE `invoices`
  MODIFY `id_invoice` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id_notif` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `id_order` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `order_details`
--
ALTER TABLE `order_details`
  MODIFY `id_detail` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `payments`
--
ALTER TABLE `payments`
  MODIFY `id_payment` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `id_product` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `product_images`
--
ALTER TABLE `product_images`
  MODIFY `id_image` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `product_variants`
--
ALTER TABLE `product_variants`
  MODIFY `id_variant` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `purchase_orders`
--
ALTER TABLE `purchase_orders`
  MODIFY `id_purchase_order` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `purchase_order_items`
--
ALTER TABLE `purchase_order_items`
  MODIFY `id_item` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `shipments`
--
ALTER TABLE `shipments`
  MODIFY `id_ship` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `stock`
--
ALTER TABLE `stock`
  MODIFY `id_stock` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=36;

--
-- AUTO_INCREMENT for table `stock_movements`
--
ALTER TABLE `stock_movements`
  MODIFY `id_movement` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `suppliers`
--
ALTER TABLE `suppliers`
  MODIFY `id_supplier` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `transporteurs`
--
ALTER TABLE `transporteurs`
  MODIFY `id_transporteur` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id_user` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `wishlists`
--
ALTER TABLE `wishlists`
  MODIFY `id_wishlist` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `wishlist_items`
--
ALTER TABLE `wishlist_items`
  MODIFY `id_wishlist_item` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `addresses`
--
ALTER TABLE `addresses`
  ADD CONSTRAINT `FK1fa36y2oqhao3wgg2rw1pi459` FOREIGN KEY (`user_id`) REFERENCES `users` (`id_user`);

--
-- Constraints for table `carts`
--
ALTER TABLE `carts`
  ADD CONSTRAINT `FKb5o626f86h46m4s7ms6ginnop` FOREIGN KEY (`user_id`) REFERENCES `users` (`id_user`);

--
-- Constraints for table `cart_items`
--
ALTER TABLE `cart_items`
  ADD CONSTRAINT `FK5yyw1o0dor9gmxfra1dqvn4qa` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id_variant`),
  ADD CONSTRAINT `FKpcttvuq4mxppo8sxggjtn5i2c` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id_cart`);

--
-- Constraints for table `chatbot_messages`
--
ALTER TABLE `chatbot_messages`
  ADD CONSTRAINT `FK7b4xj26r6rgyoq77u8vd4gskr` FOREIGN KEY (`user_id`) REFERENCES `users` (`id_user`);

--
-- Constraints for table `clients`
--
ALTER TABLE `clients`
  ADD CONSTRAINT `FKtiuqdledq2lybrds2k3rfqrv4` FOREIGN KEY (`user_id`) REFERENCES `users` (`id_user`);

--
-- Constraints for table `invoices`
--
ALTER TABLE `invoices`
  ADD CONSTRAINT `FK4ko3y00tkkk2ya3p6wnefjj2f` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id_order`),
  ADD CONSTRAINT `FKbwr4d4vyqf2bkoetxtt8j9dx7` FOREIGN KEY (`user_id`) REFERENCES `users` (`id_user`);

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `FK9y21adhxn0ayjhfocscqox7bh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id_user`);

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`id_user`),
  ADD CONSTRAINT `FKm2dep9derpoaehshbkkatam3v` FOREIGN KEY (`client_id`) REFERENCES `clients` (`id_client`);

--
-- Constraints for table `order_details`
--
ALTER TABLE `order_details`
  ADD CONSTRAINT `FK4q98utpd73imf4yhttm3w0eax` FOREIGN KEY (`product_id`) REFERENCES `products` (`id_product`),
  ADD CONSTRAINT `FK63mqrfva3vf2gx2sublm4m51w` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id_variant`),
  ADD CONSTRAINT `FKjyu2qbqt8gnvno9oe9j2s2ldk` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id_order`);

--
-- Constraints for table `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `FK81gagumt0r8y3rmudcgpbk42l` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id_order`);

--
-- Constraints for table `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id_category`);

--
-- Constraints for table `product_images`
--
ALTER TABLE `product_images`
  ADD CONSTRAINT `FKqnq71xsohugpqwf3c9gxmsuy` FOREIGN KEY (`product_id`) REFERENCES `products` (`id_product`);

--
-- Constraints for table `product_variants`
--
ALTER TABLE `product_variants`
  ADD CONSTRAINT `FKosqitn4s405cynmhb87lkvuau` FOREIGN KEY (`product_id`) REFERENCES `products` (`id_product`);

--
-- Constraints for table `purchase_orders`
--
ALTER TABLE `purchase_orders`
  ADD CONSTRAINT `FKcgqvocxciov7fq8tev9p50e5d` FOREIGN KEY (`created_by`) REFERENCES `users` (`id_user`),
  ADD CONSTRAINT `FKrpdasmb8y8xs5tiy4369xpinq` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id_supplier`);

--
-- Constraints for table `purchase_order_items`
--
ALTER TABLE `purchase_order_items`
  ADD CONSTRAINT `FK8vvrbcsu89a1ddsrfygegjio` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id_variant`),
  ADD CONSTRAINT `FKo3yj8ocbw2kav38548t22hgh8` FOREIGN KEY (`purchase_order_id`) REFERENCES `purchase_orders` (`id_purchase_order`);

--
-- Constraints for table `shipments`
--
ALTER TABLE `shipments`
  ADD CONSTRAINT `FKplhb19s5v79q0s2ra80eyts0f` FOREIGN KEY (`shipping_address_id`) REFERENCES `addresses` (`id_address`),
  ADD CONSTRAINT `FKrnt4wht95lxxplspltrg9681s` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id_order`);

--
-- Constraints for table `stock`
--
ALTER TABLE `stock`
  ADD CONSTRAINT `FKeuiihog7wq4cu7nvqu7jx57d2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id_product`),
  ADD CONSTRAINT `FKql2vw116ep463xsdo3slus78m` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id_variant`);

--
-- Constraints for table `stock_movements`
--
ALTER TABLE `stock_movements`
  ADD CONSTRAINT `FKfqq1iu0gt0la6ruk2o62bry5v` FOREIGN KEY (`user_id`) REFERENCES `users` (`id_user`),
  ADD CONSTRAINT `FKo3rdw1xgft64g9fr8d3rnbt1q` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id_variant`);

--
-- Constraints for table `wishlists`
--
ALTER TABLE `wishlists`
  ADD CONSTRAINT `FK330pyw2el06fn5g28ypyljt16` FOREIGN KEY (`user_id`) REFERENCES `users` (`id_user`);

--
-- Constraints for table `wishlist_items`
--
ALTER TABLE `wishlist_items`
  ADD CONSTRAINT `FKd20mh8fp7bij1shxeor4vwwe` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id_variant`),
  ADD CONSTRAINT `FKkem9l8vd14pk3cc4elnpl0n00` FOREIGN KEY (`wishlist_id`) REFERENCES `wishlists` (`id_wishlist`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
