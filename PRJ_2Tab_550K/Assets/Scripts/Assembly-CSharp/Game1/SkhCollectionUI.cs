using System;
using Game1.Assets.src.g;

namespace Game1
{
	public static class SkhSlotEffect
	{
		public static int GetCanonicalSkhId(int optId)
		{
			switch (optId)
			{
			case 127:
			case 139:
				return 127; // Thên Xin Hăng
			case 128:
			case 140:
				return 128; // Kirin
			case 129:
			case 141:
				return 129; // Sôngôku
			case 130:
			case 142:
				return 130; // Picolo
			case 131:
			case 143:
				return 131; // Ốc Tiêu
			case 132:
			case 144:
				return 132; // Pikkoro Daimao
			case 133:
			case 136:
				return 133; // Kakarot
			case 134:
			case 137:
				return 134; // Ca Đíc
			case 135:
			case 138:
				return 135; // Nappa
			case 233:
			case 234:
				return 233; // Gohan
			case 237:
			case 238:
			case 239:
			case 240:
				return 237; // Nail (Mới)
			case 241:
			case 242:
			case 243:
			case 244:
				return 241; // Cađic M (Mới)
			case 245:
			case 246:
			case 247:
			case 248:
				return 245; // Kaio (Mới)
			default:
				return -1;
			}
		}

		public static int GetItemSkhId(Item item)
		{
			if (item == null || item.itemOption == null)
			{
				return -1;
			}
			for (int i = 0; i < item.itemOption.Length; i++)
			{
				ItemOption io = item.itemOption[i];
				if (io != null && io.optionTemplate != null)
				{
					int skh = GetCanonicalSkhId(io.optionTemplate.id);
					if (skh != -1)
					{
						return skh;
					}
				}
			}
			return -1;
		}

		public static bool IsNewSkhSet(int skhId)
		{
			return skhId == 237 || skhId == 241 || skhId == 245;
		}

		public static bool IsDoThanLinh(Item item)
		{
			if (item == null || item.template == null)
			{
				return false;
			}
			int id = item.template.id;
			return id >= 555 && id <= 567;
		}

		public static bool IsDoHuyDiet(Item item)
		{
			if (item == null || item.template == null)
			{
				return false;
			}
			int id = item.template.id;
			return id >= 650 && id <= 662;
		}

		public static void GetSetColors(int skhId, out int coreColor, out int mainColor, out int trailColor, out int pastelBg)
		{
			pastelBg = -1;
			switch (skhId)
			{
			case 129: // Sôngôku - Vàng Kim Hoàng Gia
				coreColor = 0xFFFFF0;
				mainColor = 0xFFD700;
				trailColor = 0xFFA000;
				break;
			case 128: // Kirin - Xanh Lam Kamehameha
				coreColor = 0xE8FFFF;
				mainColor = 0x00E5FF;
				trailColor = 0x0091EA;
				break;
			case 127: // Thên Xin Hăng - Cam Thái Dương Hạ San
				coreColor = 0xFFFDE7;
				mainColor = 0xFFAB40;
				trailColor = 0xFF6D00;
				break;
			case 130: // Picolo - Tím Ma Quang
				coreColor = 0xF8E8FF;
				mainColor = 0xE040FB;
				trailColor = 0x8E24AA;
				break;
			case 131: // Ốc Tiêu - Xanh Ngọc Lục Bảo
				coreColor = 0xE8FFF2;
				mainColor = 0x00E676;
				trailColor = 0x00A854;
				break;
			case 132: // Pikkoro Daimao - Đỏ Huyết Đệ Tử
				coreColor = 0xFFEBEB;
				mainColor = 0xFF5252;
				trailColor = 0xC62828;
				break;
			case 133: // Kakarot - Cam Đỏ Rực Cháy
				coreColor = 0xFFF3E0;
				mainColor = 0xFF6E40;
				trailColor = 0xDD2C00;
				break;
			case 134: // Ca Đíc - Xanh Dương Hoàng Tộc Xayda
				coreColor = 0xE8F0FF;
				mainColor = 0x448AFF;
				trailColor = 0x1565C0;
				break;
			case 135: // Nappa - Vàng Đồng Chiến Giáp
				coreColor = 0xFFF9E6;
				mainColor = 0xFFCA28;
				trailColor = 0xC68400;
				break;
			case 233: // Gohan - Xanh Chanh Kim Quang May Mắn
				coreColor = 0xF9FFE6;
				mainColor = 0xC6FF00;
				trailColor = 0x64DD17;
				break;
			case 245: // Kaio (Mới) - Nền Hồng Phấn Nhạt + Điện Đỏ Hồng Kaioken
				coreColor = 0xFFF0F5;
				mainColor = 0xFF4081;
				trailColor = 0xF50057;
				pastelBg = 0xFFCDD2;
				break;
			case 237: // Nail (Mới) - Nền Xanh Bạc Hà Nhạt + Điện Ngọc Bích
				coreColor = 0xE8FFFA;
				mainColor = 0x1DE9B6;
				trailColor = 0x00BFA5;
				pastelBg = 0xB2DFDB;
				break;
			case 241: // Cađic M (Mới) - Nền Tím Oải Hương Nhạt + Điện Tím Majin
				coreColor = 0xF3E8FF;
				mainColor = 0xB388FF;
				trailColor = 0x7C4DFF;
				pastelBg = 0xD1C4E9;
				break;
			default:
				coreColor = 0xFFFFFF;
				mainColor = 0x00E5FF;
				trailColor = 0x0099CC;
				break;
			}
		}

		public static void PaintSlotBg(mGraphics g, Item item, int x, int y, int w, int h)
		{
			if (item == null || w <= 4 || h <= 4)
			{
				return;
			}
			int skhId = GetItemSkhId(item);
			if (skhId == -1)
			{
				return;
			}
			GetSetColors(skhId, out int _, out int mainColor, out int _, out int pastelBg);
			bool isTL = IsDoThanLinh(item);
			bool isHD = IsDoHuyDiet(item);

			if (isHD)
			{
				// Đồ Hủy Diệt SKH: Nền Neon Tím Hồng trong suốt nhẹ (tôn icon trang bị nổi bật)
				g.setColor(0xE040FB, 0.20f);
				g.fillRect(x + 1, y + 1, w - 2, h - 2);
				g.setColor(0x7C4DFF, 0.12f);
				g.fillRect(x + 3, y + 3, w - 6, h - 6);
				g.setColor(0xFF4081, 0.65f);
				g.drawRect(x + 1, y + 1, w - 3, h - 3);
			}
			else if (isTL)
			{
				// Đồ Thần Linh SKH: Nền Neon Xanh Ngọc - Ánh Kim trong suốt nhẹ
				g.setColor(0x00F5FF, 0.18f);
				g.fillRect(x + 1, y + 1, w - 2, h - 2);
				g.setColor(0xFFE57F, 0.10f);
				g.fillRect(x + 3, y + 3, w - 6, h - 6);
				g.setColor(0x00E5FF, 0.60f);
				g.drawRect(x + 1, y + 1, w - 3, h - 3);
			}
			else if (IsNewSkhSet(skhId) && pastelBg != -1)
			{
				// Set Kích Hoạt Mới: Nền màu nhạt thanh lịch
				g.setColor(pastelBg, 0.34f);
				g.fillRect(x + 1, y + 1, w - 2, h - 2);
				g.setColor(mainColor, 0.28f);
				g.drawRect(x + 1, y + 1, w - 3, h - 3);
			}
		}

		public static void PaintSlotBorder(mGraphics g, Item item, int x, int y, int w, int h)
		{
			if (item == null || w <= 6 || h <= 6)
			{
				return;
			}
			int skhId = GetItemSkhId(item);
			if (skhId == -1)
			{
				return;
			}
			PaintElectricBorderBySkhId(g, skhId, IsDoThanLinh(item), IsDoHuyDiet(item), x, y, w, h);
		}

		public static void PaintElectricBorderBySkhId(mGraphics g, int skhId, bool isTL, bool isHD, int x, int y, int w, int h)
		{
			GetSetColors(skhId, out int coreColor, out int mainColor, out int trailColor, out int _);

			// Viền mảnh nhẹ theo màu Set
			g.setColor(trailColor, (isTL || isHD) ? 0.55f : 0.35f);
			g.drawRect(x, y, w - 1, h - 1);

			int perim = (w + h) * 2;
			if (perim <= 8)
			{
				return;
			}

			int cometCount = isHD ? 4 : ((isTL || IsNewSkhSet(skhId)) ? 3 : 2);
			int speed = isHD ? 3 : 2;
			int baseTick = GameCanvas.gameTick * speed;
			int spacing = perim / cometCount;

			for (int c = 0; c < cometCount; c++)
			{
				int headPos = (baseTick + c * spacing) % perim;
				// Vẽ đuôi tia điện chạy quanh viền ô
				for (int seg = 5; seg >= 0; seg--)
				{
					int pos = (headPos - seg * 2 + perim * 4) % perim;
					GetPerimeterPoint(pos, x, y, w, h, out int px, out int py);

					// Tạo độ giật tia điện nhỏ ở thân tia
					if (seg > 0 && seg < 4 && ((GameCanvas.gameTick + c + seg) % 3 == 0))
					{
						px += ((seg % 2 == 0) ? 1 : -1);
					}

					if (seg == 0)
					{
						g.setColor(mainColor, 0.85f);
						g.fillRect(px - 1, py - 1, 3, 3);
						g.setColor(coreColor);
						g.fillRect(px, py, 2, 2);
					}
					else if (seg <= 2)
					{
						g.setColor(mainColor, 0.90f);
						g.fillRect(px, py, 2, 2);
					}
					else
					{
						g.setColor(trailColor, 0.65f);
						g.fillRect(px, py, 1, 1);
					}
				}
			}
		}

		private static void GetPerimeterPoint(int pos, int x, int y, int w, int h, out int px, out int py)
		{
			if (pos < w)
			{
				px = x + pos;
				py = y;
			}
			else if (pos < w + h)
			{
				px = x + w - 1;
				py = y + (pos - w);
			}
			else if (pos < w * 2 + h)
			{
				px = x + w - 1 - (pos - (w + h));
				py = y + h - 1;
			}
			else
			{
				px = x;
				py = y + h - 1 - (pos - (w * 2 + h));
			}
		}
	}

	public class SkhCollectionUI
	{
		public const sbyte CMD_SKH_COLLECTION = 118;

		private const int NUM_SETS = 5;
		private const int NUM_SLOTS = 5;

		private static readonly string[] SLOT_LABELS = new string[5] { "Áo", "Quần", "Găng", "Giày", "Rada" };
		private static readonly short[,] PLACEHOLDER_ICONS = new short[3, 5]
		{
			{ 0, 6, 21, 27, 12 },   // Trái Đất starter item IDs -> looked up via ItemTemplates
			{ 1, 7, 22, 28, 12 },   // Namek
			{ 2, 8, 23, 29, 12 }    // Xayda
		};

		private static SkhCollectionUI instance;

		public bool isShow;
		public int activeTab;
		public int selectedSlot;
		public int completedSetCount;
		public bool isInHouse;

		private readonly string[] tabNames = new string[NUM_SETS];
		private readonly int[] tabSkhOptionIds = new int[NUM_SETS];
		private readonly int[] tabMountedCounts = new int[NUM_SETS];
		private readonly Item[,] mountedItems = new Item[NUM_SETS, NUM_SLOTS];

		public static SkhCollectionUI GI()
		{
			if (instance == null)
			{
				instance = new SkhCollectionUI();
			}
			return instance;
		}

		public SkhCollectionUI()
		{
			for (int t = 0; t < NUM_SETS; t++)
			{
				tabNames[t] = "Set " + (t + 1);
				tabSkhOptionIds[t] = 129;
			}
		}

		public void Close()
		{
			isShow = false;
			GameCanvas.clearKeyPressed();
			GameCanvas.clearKeyHold();
		}

		public void ReadMessage(Message msg)
		{
			try
			{
				sbyte sub = msg.reader().readByte();
				if (sub != 0)
				{
					return;
				}
				activeTab = msg.reader().readByte();
				if (activeTab < 0 || activeTab >= NUM_SETS)
				{
					activeTab = 0;
				}
				completedSetCount = msg.reader().readByte();
				isInHouse = msg.reader().readBoolean();

				for (int t = 0; t < NUM_SETS; t++)
				{
					tabNames[t] = msg.reader().readUTF();
					tabSkhOptionIds[t] = msg.reader().readShort();
					tabMountedCounts[t] = msg.reader().readByte();
					for (int s = 0; s < NUM_SLOTS; s++)
					{
						short tempId = msg.reader().readShort();
						if (tempId != -1)
						{
							short iconId = msg.reader().readShort();
							string itemName = msg.reader().readUTF();
							int optCount = msg.reader().readUnsignedByte();

							Item it = new Item();
							ItemTemplate tpl = ItemTemplates.get(tempId);
							if (tpl != null)
							{
								it.template = tpl;
							}
							else
							{
								it.template = new ItemTemplate(tempId, (sbyte)s, 0, itemName, string.Empty, 0, 0, iconId, -1, false);
							}
							it.quantity = 1;
							it.itemOption = new ItemOption[optCount];
							for (int k = 0; k < optCount; k++)
							{
								int optId = msg.reader().readShort();
								int param = msg.reader().readInt();
								it.itemOption[k] = new ItemOption(optId, param);
							}
							mountedItems[t, s] = it;
						}
						else
						{
							mountedItems[t, s] = null;
						}
					}
				}

				if (GameCanvas.panel != null && GameCanvas.panel.isShow)
				{
					GameCanvas.panel.hide();
				}
				isShow = true;
			}
			catch (Exception)
			{
			}
		}

		private void SendAction(int action, int arg1 = -1, int arg2 = -1)
		{
			Message m = null;
			try
			{
				m = new Message(CMD_SKH_COLLECTION);
				m.writer().writeByte(action);
				if (arg1 != -1)
				{
					m.writer().writeByte(arg1);
				}
				if (arg2 != -1)
				{
					m.writer().writeByte(arg2);
				}
				Session_ME.gI().sendMessage(m);
			}
			catch (Exception)
			{
			}
			finally
			{
				if (m != null)
				{
					m.cleanup();
				}
			}
		}

		private void ComputeLayout(
			out int uiX, out int uiY, out int uiW, out int uiH,
			out int leftX, out int bodyY, out int leftW,
			out int tabW, out int tabH,
			out int rackX, out int rackY, out int rackW, out int rackH,
			out int scrollX, out int scrollY, out int scrollW, out int scrollH,
			out int btnY, out int btnH)
		{
			uiW = System.Math.Min(416, GameCanvas.w - 8);
			uiH = System.Math.Min(232, GameCanvas.h - 6);
			uiX = (GameCanvas.w - uiW) / 2;
			uiY = (GameCanvas.h - uiH) / 2;

			bodyY = uiY + 24;
			scrollW = System.Math.Min(138, uiW * 35 / 100);
			leftW = uiW - scrollW - 8;
			leftX = uiX;

			tabW = 66;
			tabH = 27;

			rackX = leftX + tabW - 2;
			rackY = bodyY + 18;
			rackW = leftW - tabW + 2;
			rackH = 152;

			scrollX = leftX + leftW + 8;
			scrollY = bodyY + 2;
			scrollH = 166;

			btnY = rackY + rackH + 5;
			btnH = 22;
		}

		public void UpdateKey()
		{
			if (!isShow)
			{
				return;
			}

			ComputeLayout(
				out int uiX, out int uiY, out int uiW, out int uiH,
				out int leftX, out int bodyY, out int leftW,
				out int tabW, out int tabH,
				out int rackX, out int rackY, out int rackW, out int rackH,
				out int scrollX, out int scrollY, out int scrollW, out int scrollH,
				out int btnY, out int btnH);

			// Phím tắt bàn phím PC
			if (GameCanvas.keyPressed[2]) // Up
			{
				GameCanvas.keyPressed[2] = false;
				selectedSlot = (selectedSlot + NUM_SLOTS - 1) % NUM_SLOTS;
				return;
			}
			if (GameCanvas.keyPressed[8]) // Down
			{
				GameCanvas.keyPressed[8] = false;
				selectedSlot = (selectedSlot + 1) % NUM_SLOTS;
				return;
			}
			if (GameCanvas.keyPressed[4]) // Left
			{
				GameCanvas.keyPressed[4] = false;
				activeTab = (activeTab + NUM_SETS - 1) % NUM_SETS;
				return;
			}
			if (GameCanvas.keyPressed[6]) // Right
			{
				GameCanvas.keyPressed[6] = false;
				activeTab = (activeTab + 1) % NUM_SETS;
				return;
			}
			if (GameCanvas.keyPressed[5]) // Enter -> Gắn đồ
			{
				GameCanvas.keyPressed[5] = false;
				SendAction(1, activeTab);
				return;
			}
			if (GameCanvas.keyPressed[12] || GameCanvas.keyPressed[13])
			{
				GameCanvas.keyPressed[12] = false;
				GameCanvas.keyPressed[13] = false;
				Close();
				return;
			}

			if (!GameCanvas.isPointerJustRelease || !GameCanvas.isPointerClick)
			{
				return;
			}

			// Click chọn 1 trong 5 Tab Set bên trái
			for (int t = 0; t < NUM_SETS; t++)
			{
				int ty = rackY + 4 + t * (tabH + 2);
				if (GameCanvas.isPointerHoldIn(leftX, ty, tabW, tabH))
				{
					activeTab = t;
					GameCanvas.clearAllPointerEvent();
					return;
				}
			}

			// Click chọn 1 trong 5 Ô Trang Bị (Áo, Quần, Găng, Giày, Rada)
			for (int s = 0; s < NUM_SLOTS; s++)
			{
				int sy = rackY + 6 + s * 28;
				if (GameCanvas.isPointerHoldIn(rackX + 6, sy, rackW - 12, 26))
				{
					selectedSlot = s;
					GameCanvas.clearAllPointerEvent();
					return;
				}
			}

			// 4 Nút chức năng phía dưới: GẮN ĐỒ | THÁO ĐỒ | CẨM NANG | THOÁT
			int btnGap = 5;
			int btnW = System.Math.Min(64, (leftW - btnGap * 3) / 4);
			int startBtnX = leftX + (leftW - (btnW * 4 + btnGap * 3)) / 2;

			// Nút 0: GẮN ĐỒ
			if (GameCanvas.isPointerHoldIn(startBtnX, btnY, btnW, btnH))
			{
				GameCanvas.clearAllPointerEvent();
				SendAction(1, activeTab);
				return;
			}

			// Nút 1: THÁO ĐỒ
			if (GameCanvas.isPointerHoldIn(startBtnX + (btnW + btnGap), btnY, btnW, btnH))
			{
				GameCanvas.clearAllPointerEvent();
				if (mountedItems[activeTab, selectedSlot] != null)
				{
					SendAction(2, activeTab, selectedSlot);
				}
				else
				{
					GameScr.info1.addInfo("Ô [" + SLOT_LABELS[selectedSlot] + "] đang trống!", 0);
				}
				return;
			}

			// Nút 2: CẨM NANG
			if (GameCanvas.isPointerHoldIn(startBtnX + (btnW + btnGap) * 2, btnY, btnW, btnH))
			{
				GameCanvas.clearAllPointerEvent();
				SendAction(3);
				return;
			}

			// Nút 3: THOÁT
			if (GameCanvas.isPointerHoldIn(startBtnX + (btnW + btnGap) * 3, btnY, btnW, btnH))
			{
				GameCanvas.clearAllPointerEvent();
				Close();
				return;
			}

			// Click ra ngoài vùng bảng -> đóng bảng nhanh
			if (!GameCanvas.isPointerHoldIn(uiX - 4, uiY - 4, uiW + 8, uiH + 8))
			{
				GameCanvas.clearAllPointerEvent();
				Close();
				return;
			}

			GameCanvas.clearAllPointerEvent();
		}

		public void Paint(mGraphics g)
		{
			if (!isShow)
			{
				return;
			}

			g.translate(-g.getTranslateX(), -g.getTranslateY());
			g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

			// Lớp nền tối mờ để làm nổi bật khung gỗ trên nền Nhà
			g.setColor(0x000000, 0.56f);
			g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

			ComputeLayout(
				out int uiX, out int uiY, out int uiW, out int uiH,
				out int leftX, out int bodyY, out int leftW,
				out int tabW, out int tabH,
				out int rackX, out int rackY, out int rackW, out int rackH,
				out int scrollX, out int scrollY, out int scrollW, out int scrollH,
				out int btnY, out int btnH);

			// 1. Bảng gỗ tiêu đề trên cùng: SƯU TẬP SET KÍCH HOẠT
			int titleW = System.Math.Min(236, uiW - 20);
			int titleX = uiX + (uiW - titleW) / 2;
			PaintWoodBox(g, titleX, uiY, titleW, 21, 0x5B3516, 0x7E4B22, 0x261306);
			PaintGoldRivets(g, titleX, uiY, titleW, 21);
			mFont.tahoma_7b_yellow.drawString(g, "SƯU TẬP SET KÍCH HOẠT", titleX + titleW / 2, uiY + 5, mFont.CENTER);

			// 2. Thanh tiêu đề phụ khung trái: BẢNG GẮN ĐỒ & TAB (5 SET)
			PaintWoodBox(g, leftX + 6, bodyY, leftW - 10, 19, 0x472911, 0x663B19, 0x241205);
			mFont.tahoma_7b_white.drawString(g, "BẢNG GẮN ĐỒ & TAB (5 SET)", leftX + 6 + (leftW - 10) / 2, bodyY + 4, mFont.CENTER);

			// 3. Các Tab không được chọn (vẽ trước để nằm dưới mép bảng gỗ)
			for (int t = 0; t < NUM_SETS; t++)
			{
				if (t != activeTab)
				{
					PaintSetTab(g, t, leftX, rackY + 4 + t * (tabH + 2), tabW, tabH, isSelected: false);
				}
			}

			// 4. Bảng Gỗ Gắn Đồ chính
			PaintWoodBox(g, rackX, rackY, rackW, rackH, 0x7A4A21, 0x9B6334, 0x2D1708);

			// Vẽ Tab đang chọn đè liền mạch vào bảng gỗ
			int activeTabY = rackY + 4 + activeTab * (tabH + 2);
			PaintSetTab(g, activeTab, leftX, activeTabY, tabW + 3, tabH, isSelected: true);
			// Xóa đường viền ngăn cách giữa Tab đang chọn và Bảng gỗ để liền khối như ảnh mẫu
			g.setColor(0x7A4A21);
			g.fillRect(rackX, activeTabY + 2, 3, tabH - 4);

			// 5. Vẽ 5 Ô Trang Bị (Áo, Quần, Găng, Giày, Rada) dọc bên trái + thông tin bên phải
			int charGender = (Char.myCharz() != null && Char.myCharz().cgender >= 0 && Char.myCharz().cgender <= 2)
				? Char.myCharz().cgender
				: 0;

			for (int s = 0; s < NUM_SLOTS; s++)
			{
				int slotX = rackX + 8;
				int slotY = rackY + 6 + s * 28;
				int slotSize = 25;
				bool isSelSlot = (s == selectedSlot);
				Item mounted = mountedItems[activeTab, s];

				// Dòng nền bên cạnh ô đồ để dễ nhìn món đang chọn
				if (isSelSlot)
				{
					g.setColor(0xFFE082, 0.16f);
					g.fillRect(rackX + 5, slotY - 1, rackW - 10, slotSize + 2);
				}

				// Khung ô kem sáng viền gỗ như mockup
				g.setColor(isSelSlot ? 0xFFD54F : 0xEFE3C6);
				g.fillRect(slotX - 1, slotY - 1, slotSize + 2, slotSize + 2);
				g.setColor(mounted != null ? 0xB89F72 : 0xCBB58B);
				g.fillRect(slotX, slotY, slotSize, slotSize);
				g.setColor(0x4A2C11);
				g.drawRect(slotX, slotY, slotSize - 1, slotSize - 1);

				int textX = slotX + slotSize + 7;
				if (mounted != null && mounted.template != null)
				{
					SkhSlotEffect.PaintSlotBg(g, mounted, slotX, slotY, slotSize, slotSize);
					SmallImage.drawSmallImage(g, mounted.template.iconID, slotX + slotSize / 2, slotY + slotSize / 2, 0, 3);
					SkhSlotEffect.PaintSlotBorder(g, mounted, slotX, slotY, slotSize, slotSize);

					mFont nameFont = SkhSlotEffect.IsDoHuyDiet(mounted)
						? mFont.tahoma_7b_pink
						: (SkhSlotEffect.IsDoThanLinh(mounted) ? mFont.tahoma_7b_yellow : mFont.tahoma_7b_green2);
					nameFont.drawString(g, mounted.template.name, textX, slotY + 2, mFont.LEFT);
					mFont.tahoma_7_yellow.drawString(g, "Đã gắn (" + SLOT_LABELS[s] + ")", textX, slotY + 13, mFont.LEFT);
				}
				else
				{
					// Vẽ icon phôi mờ làm bóng gợi ý loại trang bị
					short sampleTplId = PLACEHOLDER_ICONS[charGender, s];
					ItemTemplate sampleTpl = ItemTemplates.get(sampleTplId);
					if (sampleTpl != null)
					{
						SmallImage.drawSmallImage(g, sampleTpl.iconID, slotX + slotSize / 2, slotY + slotSize / 2, 0, 3);
						g.setColor(0x5B3A1A, 0.58f);
						g.fillRect(slotX + 1, slotY + 1, slotSize - 2, slotSize - 2);
					}
					mFont.tahoma_7_white.drawString(g, "Ô " + SLOT_LABELS[s], textX, slotY + 2, mFont.LEFT);
					mFont.tahoma_7_grey.drawString(g, isInHouse ? "Chưa gắn đồ" : "Về Nhà để gắn", textX, slotY + 13, mFont.LEFT);
				}
			}

			// 6. Cuộn Giấy Da (Parchment Scroll) bên phải: THÀNH TỰU ĐỦ BỘ KÍCH HOẠT
			PaintParchmentScroll(g, scrollX, scrollY, scrollW, scrollH);

			int curCount = tabMountedCounts[activeTab];
			bool isTabFull = (curCount >= NUM_SLOTS);
			int scCenterX = scrollX + scrollW / 2;

			mFont.tahoma_7b_dark.drawString(g, "THÀNH TỰU:", scCenterX, scrollY + 12, mFont.CENTER);
			mFont.tahoma_7b_red.drawString(g, GetShortSetTitle(activeTab), scCenterX, scrollY + 23, mFont.CENTER);

			// Thanh tiến độ x/5
			int barW = scrollW - 20;
			int barH = 12;
			int barX = scrollX + 10;
			int barY = scrollY + 37;
			g.setColor(0x2C3E50);
			g.fillRect(barX - 1, barY - 1, barW + 2, barH + 2);
			g.setColor(0x5D6D7E);
			g.fillRect(barX, barY, barW, barH);
			if (curCount > 0)
			{
				int fillW = barW * curCount / NUM_SLOTS;
				g.setColor(isTabFull ? 0x27AE60 : 0x2980B9);
				g.fillRect(barX, barY, fillW, barH);
				g.setColor(isTabFull ? 0x58D68D : 0x5DADE2, 0.55f);
				g.fillRect(barX, barY, fillW, barH / 2);
			}
			mFont.tahoma_7b_white.drawString(g, curCount + "/5", scCenterX, barY + 1, mFont.CENTER);

			// Các dòng chỉ số thưởng của mỗi Set
			int statX = scrollX + 8;
			int statY = scrollY + 55;
			mFont.tahoma_7b_dark.drawString(g, "+3% Sức đánh", statX + 1, statY + 1, mFont.LEFT);
			mFont.tahoma_7b_yellow.drawString(g, "+3% Sức đánh", statX, statY, mFont.LEFT);

			mFont.tahoma_7b_dark.drawString(g, "+3% HP", statX + 1, statY + 14, mFont.LEFT);
			mFont.tahoma_7b_green.drawString(g, "+3% HP", statX, statY + 13, mFont.LEFT);

			mFont.tahoma_7b_dark.drawString(g, "+3% KI", statX + 1, statY + 27, mFont.LEFT);
			mFont.tahoma_7b_blue.drawString(g, "+3% KI", statX, statY + 26, mFont.LEFT);

			mFont.tahoma_7b_dark.drawString(g, "+15% TNSM từ", statX, statY + 41, mFont.LEFT);
			mFont.tahoma_7b_dark.drawString(g, "Sư phụ & Đệ tử", statX, statY + 52, mFont.LEFT);

			// Vẽ Cúp Vàng bên phải dòng HP/KI giống ảnh mẫu
			PaintGoldenTrophy(g, scrollX + scrollW - 30, statY + 10, isTabFull);

			// Tổng cộng dồn cả 5 Set ở chân cuộn giấy
			g.setColor(0x8D6E43, 0.55f);
			g.fillRect(scrollX + 8, scrollY + scrollH - 35, scrollW - 16, 1);
			int totalStatPct = completedSetCount * 3;
			int totalTnsmPct = completedSetCount * 15;
			mFont.tahoma_7b_dark.drawString(g, "Tổng đủ: " + completedSetCount + "/5 Bộ", scCenterX, scrollY + scrollH - 32, mFont.CENTER);
			mFont.tahoma_7_blue1.drawString(g, "+" + totalStatPct + "% CS | +" + totalTnsmPct + "% TNSM", scCenterX, scrollY + scrollH - 21, mFont.CENTER);

			// Dòng trạng thái kích hoạt bên dưới cuộn giấy (như ảnh mẫu)
			int statusY = scrollY + scrollH + 3;
			if (isTabFull)
			{
				mFont.tahoma_7_white.drawString(g, "(Kích hoạt 100%)", scCenterX, statusY, mFont.CENTER);
				mFont.tahoma_7b_green2.drawString(g, "ĐÃ KÍCH HOẠT", scCenterX, statusY + 11, mFont.CENTER);
			}
			else
			{
				mFont.tahoma_7_yellow.drawString(g, "(Tiến độ: " + curCount + "/5 món)", scCenterX, statusY, mFont.CENTER);
				mFont.tahoma_7_grey.drawString(g, "CHƯA ĐỦ BỘ", scCenterX, statusY + 11, mFont.CENTER);
			}

			// 7. Vẽ 4 Nút bấm phía dưới: GẮN ĐỒ | THÁO ĐỒ | CẨM NANG | THOÁT
			int btnGap = 5;
			int btnW = System.Math.Min(64, (leftW - btnGap * 3) / 4);
			int startBtnX = leftX + (leftW - (btnW * 4 + btnGap * 3)) / 2;
			bool canUnmount = (mountedItems[activeTab, selectedSlot] != null);

			PaintStylizedButton(g, startBtnX, btnY, btnW, btnH, "GẮN ĐỒ", 0x0E4D64, 0x1B7291, 0x062633, enabled: true);
			PaintStylizedButton(g, startBtnX + (btnW + btnGap), btnY, btnW, btnH, "THÁO ĐỒ",
				canUnmount ? 0x8D5524 : 0x4A3B2C,
				canUnmount ? 0xB87333 : 0x5E4C3A,
				0x261608, enabled: canUnmount);
			PaintStylizedButton(g, startBtnX + (btnW + btnGap) * 2, btnY, btnW, btnH, "CẨM NANG", 0x24476E, 0x356598, 0x102238, enabled: true);
			PaintStylizedButton(g, startBtnX + (btnW + btnGap) * 3, btnY, btnW, btnH, "THOÁT", 0xC83E1D, 0xE65A37, 0x561607, enabled: true);
		}

		private string GetShortSetTitle(int tab)
		{
			string raw = tabNames[tab] ?? ("Set " + (tab + 1));
			if (raw.Length > 18)
			{
				return raw.Substring(0, 18);
			}
			return raw.ToUpper();
		}

		private string GetShortTabLabel(int tab)
		{
			string raw = tabNames[tab] ?? ("Set " + (tab + 1));
			if (raw.StartsWith("Set "))
			{
				raw = raw.Substring(4);
			}
			if (raw == "Thên Xin Hăng")
			{
				return "T.X.Hăng";
			}
			if (raw == "Pikkoro Daimao")
			{
				return "Pikkoro";
			}
			return raw;
		}

		private void PaintSetTab(mGraphics g, int tabIndex, int x, int y, int w, int h, bool isSelected)
		{
			int bg = isSelected ? 0x7A4A21 : 0x523015;
			int hi = isSelected ? 0x9B6334 : 0x6B3F1C;
			PaintWoodBox(g, x, y, w, h, bg, hi, 0x261306);

			int skhId = tabSkhOptionIds[tabIndex];
			SkhSlotEffect.GetSetColors(skhId, out int _, out int setMainColor, out int _, out int _);
			// Vạch màu đặc trưng của Set ở cạnh trái Tab
			g.setColor(setMainColor, isSelected ? 0.95f : 0.65f);
			g.fillRect(x + 2, y + 3, 3, h - 6);

			string label = GetShortTabLabel(tabIndex);
			mFont titleFont = isSelected ? mFont.tahoma_7b_white : mFont.tahoma_7_yellow;
			titleFont.drawString(g, label, x + 8, y + 3, mFont.LEFT);

			int count = tabMountedCounts[tabIndex];
			mFont countFont = (count >= NUM_SLOTS) ? mFont.tahoma_7b_green2 : mFont.tahoma_7_grey;
			countFont.drawString(g, count + "/5 món", x + 8, y + 14, mFont.LEFT);
		}

		private static void PaintWoodBox(mGraphics g, int x, int y, int w, int h, int fillHex, int highlightHex, int borderHex)
		{
			g.setColor(borderHex);
			g.fillRect(x, y, w, h);
			g.setColor(fillHex);
			g.fillRect(x + 1, y + 1, w - 2, h - 2);
			g.setColor(highlightHex, 0.7f);
			g.drawRect(x + 2, y + 2, w - 5, h - 5);
		}

		private static void PaintGoldRivets(mGraphics g, int x, int y, int w, int h)
		{
			g.setColor(0xFFD54F);
			g.fillRect(x + 4, y + 4, 2, 2);
			g.fillRect(x + w - 6, y + 4, 2, 2);
			g.fillRect(x + 4, y + h - 6, 2, 2);
			g.fillRect(x + w - 6, y + h - 6, 2, 2);
		}

		private static void PaintParchmentScroll(mGraphics g, int x, int y, int w, int h)
		{
			// Thân giấy da
			g.setColor(0x3E2611);
			g.fillRect(x, y + 6, w, h - 12);
			g.setColor(0xE8D2A2);
			g.fillRect(x + 1, y + 7, w - 2, h - 14);
			g.setColor(0xF5E7C6, 0.65f);
			g.fillRect(x + 4, y + 10, w - 8, h - 20);

			// Thanh cuộn trên & dưới
			int rollX = x - 3;
			int rollW = w + 6;
			int rollH = 9;
			// Cuộn trên
			g.setColor(0x3E2611);
			g.fillRect(rollX, y, rollW, rollH);
			g.setColor(0xD3B67E);
			g.fillRect(rollX + 1, y + 1, rollW - 2, rollH - 2);
			g.setColor(0xF2E0B6);
			g.fillRect(rollX + 2, y + 2, rollW - 4, 2);
			// Cuộn dưới
			int botY = y + h - rollH;
			g.setColor(0x3E2611);
			g.fillRect(rollX, botY, rollW, rollH);
			g.setColor(0xD3B67E);
			g.fillRect(rollX + 1, botY + 1, rollW - 2, rollH - 2);
			g.setColor(0xF2E0B6);
			g.fillRect(rollX + 2, botY + 2, rollW - 4, 2);
		}

		private static void PaintGoldenTrophy(mGraphics g, int x, int y, bool active)
		{
			int mainGold = active ? 0xFFC107 : 0x9E8C6C;
			int lightGold = active ? 0xFFF59D : 0xC2B59B;
			int darkGold = active ? 0xB26A00 : 0x5D5340;

			// Quai cúp hai bên
			g.setColor(darkGold);
			g.drawRect(x, y + 3, 19, 8);
			// Bầu cúp
			g.setColor(mainGold);
			g.fillRect(x + 3, y + 1, 14, 11);
			g.fillRect(x + 5, y + 12, 10, 3);
			// Ánh kim trên bầu cúp
			g.setColor(lightGold);
			g.fillRect(x + 5, y + 2, 4, 8);
			// Chân cúp & đế gỗ
			g.setColor(darkGold);
			g.fillRect(x + 8, y + 15, 4, 5);
			g.setColor(0x4E2F14);
			g.fillRect(x + 4, y + 20, 12, 4);
			g.setColor(mainGold);
			g.fillRect(x + 6, y + 21, 8, 2);

			// Hạt lấp lánh quanh cúp khi đã kích hoạt đủ 5/5
			if (active && (GameCanvas.gameTick / 6) % 2 == 0)
			{
				g.setColor(0xFFFFFF);
				g.fillRect(x + 1, y - 1, 2, 2);
				g.fillRect(x + 18, y + 13, 2, 2);
			}
		}

		private static void PaintStylizedButton(mGraphics g, int x, int y, int w, int h, string text, int fillHex, int topHiHex, int borderHex, bool enabled)
		{
			g.setColor(borderHex);
			g.fillRect(x, y, w, h);
			g.setColor(fillHex);
			g.fillRect(x + 1, y + 1, w - 2, h - 2);
			g.setColor(topHiHex);
			g.fillRect(x + 2, y + 2, w - 4, 3);

			mFont font = enabled ? mFont.tahoma_7b_yellow : mFont.tahoma_7_grey;
			if (enabled && (text == "THOÁT" || text == "CẨM NANG"))
			{
				font = mFont.tahoma_7b_white;
			}
			font.drawString(g, text, x + w / 2, y + (h - 11) / 2, mFont.CENTER);
		}
	}
}
