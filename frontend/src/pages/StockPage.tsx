// src/pages/StockPage.tsx
import React, { useEffect, useState, useMemo, useRef } from 'react';
import {
  Boxes,
  Plus,
  AlertTriangle,
  History,
  TrendingDown,
  TrendingUp,
  X,
  Edit2,
  Trash2,
  LayoutGrid,
  List,
  Camera,
  IndianRupee,
  Search,
  Calendar,
  Filter,
  ChevronDown,
  PlusCircle,
  Bell,
  Download,
  Eye,
  Image as ImageIcon,
  CheckCircle
} from 'lucide-react';
import api from '../api';
import type { StockItem, StockCategory, StockTransaction } from '../types';
import { useImageCompression } from '../hooks/useImageCompression';

interface StockPageProps {
  searchTerm: string;
  showToast: (type: 'success' | 'error' | 'info', text: string) => void;
}

// ✅ Extended Categories with more options
const stockCategories: StockCategory[] = [
  'PULSES',
  'GRAINS',
  'OILS',
  'SPICES',
  'DAIRY',
  'VEGETABLES',
  'FRUITS',
  'MEAT',
  'SEAFOOD',
  'BEVERAGES',
  'SNACKS',
  'FROZEN',
  'BAKERY',
  'OTHERS',
];

// ✅ Predefined units
const PREDEFINED_UNITS = [
  'kg',
  'g',
  'liter',
  'ml',
  'piece',
  'packet',
  'bottle',
  'dozen',
];

const categoryFallbackImages: Record<string, string> = {
  PULSES: 'https://images.unsplash.com/photo-1585995603140-5e36cf364371?auto=format&fit=crop&w=600&q=80',
  GRAINS: 'https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=600&q=80',
  OILS: 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=600&q=80',
  SPICES: 'https://images.unsplash.com/photo-1596040033229-a9821ebd058d?auto=format&fit=crop&w=600&q=80',
  DAIRY: 'https://images.unsplash.com/photo-1628088062854-d1870b4553da?auto=format&fit=crop&w=600&q=80',
  VEGETABLES: 'https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=600&q=80',
  FRUITS: 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?auto=format&fit=crop&w=600&q=80',
  MEAT: 'https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?auto=format&fit=crop&w=600&q=80',
  SEAFOOD: 'https://images.unsplash.com/photo-1584723017530-1fe4f5e8f0f1?auto=format&fit=crop&w=600&q=80',
  BEVERAGES: 'https://images.unsplash.com/photo-1544145945-f90425340c7e?auto=format&fit=crop&w=600&q=80',
  SNACKS: 'https://images.unsplash.com/photo-1621939514649-280e2ee25f60?auto=format&fit=crop&w=600&q=80',
  FROZEN: 'https://images.unsplash.com/photo-1603899122634-f086ca5f5ddd?auto=format&fit=crop&w=600&q=80',
  BAKERY: 'https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=600&q=80',
  OTHERS: 'https://images.unsplash.com/photo-1553530666-ba11a7da3888?auto=format&fit=crop&w=600&q=80',
};

export const StockPage: React.FC<StockPageProps> = ({ searchTerm: globalSearchTerm, showToast }) => {
  // Image compression hook
  const { compressImage, isCompressing } = useImageCompression();
  
  const [items, setItems] = useState<StockItem[]>([]);
  const [transactions, setTransactions] = useState<StockTransaction[]>([]);
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL');
  const [activeTab, setActiveTab] = useState<'ITEMS' | 'TRANSACTIONS' | 'LOW_STOCK'>('ITEMS');
  const [viewMode, setViewMode] = useState<'CARD' | 'TABLE'>('CARD');
  const [loading, setLoading] = useState(true);
  const [uploadingItemId, setUploadingItemId] = useState<string | null>(null);
  const [localSearchTerm, setLocalSearchTerm] = useState<string>('');

  // Image preview modal state
  const [selectedImage, setSelectedImage] = useState<{ url: string; name: string; itemId?: string } | null>(null);

  // Transaction Filters
  const [txSearchTerm, setTxSearchTerm] = useState<string>('');
  const [txDateFrom, setTxDateFrom] = useState<string>('');
  const [txDateTo, setTxDateTo] = useState<string>('');
  const [txTypeFilter, setTxTypeFilter] = useState<string>('ALL');
  const [showFilters, setShowFilters] = useState<boolean>(false);

  // Modals
  const [isItemModalOpen, setIsItemModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<StockItem | null>(null);
  const [itemForm, setItemForm] = useState<Partial<StockItem>>({
    itemName: '',
    category: 'GRAINS',
    currentStock: 0,
    unit: 'kg',
    minimumStockLevel: 10,
    unitPrice: 50,
  });

  const [customUnit, setCustomUnit] = useState<string>('');
  const [showCustomUnitInput, setShowCustomUnitInput] = useState<boolean>(false);
  const [modalSelectedFile, setModalSelectedFile] = useState<File | null>(null);

  // Adjust Stock Modal
  const [adjustModalItem, setAdjustModalItem] = useState<StockItem | null>(null);
  const [adjustType, setAdjustType] = useState<'ADD' | 'DEDUCT'>('ADD');
  const [adjustQty, setAdjustQty] = useState<number>(10);
  const [adjustNotes, setAdjustNotes] = useState<string>('');

  // ============================================
  // IMAGE PREVIEW & DOWNLOAD HANDLERS
  // ============================================
  const handleImageClick = (imageUrl: string, itemName: string, itemId?: string) => {
    setSelectedImage({ url: imageUrl, name: itemName, itemId });
  };

  const handleDownloadImage = async (imageUrl: string, fileName: string) => {
    try {
      const response = await fetch(imageUrl);
      const blob = await response.blob();
      
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = `${fileName}.webp`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      
      URL.revokeObjectURL(link.href);
      showToast('success', 'Image downloaded successfully');
    } catch (error) {
      console.error('Download failed:', error);
      showToast('error', 'Failed to download image');
    }
  };

  const handleDownloadFromModal = () => {
    if (selectedImage) {
      handleDownloadImage(selectedImage.url, selectedImage.name);
    }
  };

  // ============================================
  // SILENT IMAGE COMPRESSION UPLOAD
  // ============================================
  const handleCardImageUpload = async (itemId: string, file: File) => {
    try {
      setUploadingItemId(itemId);
      
      // SILENT COMPRESSION
      const compressedFile = await compressImage(file, {
        maxWidth: 1200,
        maxHeight: 1200,
        quality: 0.85,
        maxSizeMB: 2
      });

      const formData = new FormData();
      formData.append('images', compressedFile);
      await api.stock.item.uploadImages(itemId, formData);
      
      showToast('success', 'Stock item image uploaded successfully!');
      loadData();
    } catch (err: any) {
      showToast('error', err.message || 'Failed to upload item image');
    } finally {
      setUploadingItemId(null);
    }
  };

  const loadData = async () => {
    try {
      setLoading(true);
      const [itemList, txList] = await Promise.all([
        api.stock.item.getAll().catch(() => []),
        api.stock.transaction.getAll().catch(() => []),
      ]);
      setItems(Array.isArray(itemList) ? itemList : []);
      setTransactions(Array.isArray(txList) ? txList : []);
    } catch (err: any) {
      showToast('error', err.message || 'Failed to load stock data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // Filtered Transactions
  const filteredTransactions = useMemo(() => {
    return transactions.filter(tx => {
      if (txTypeFilter !== 'ALL' && tx.transactionType !== txTypeFilter) {
        return false;
      }
      if (txSearchTerm) {
        const searchLower = txSearchTerm.toLowerCase();
        const itemName = tx.stockItemName || tx.itemName || '';
        if (!itemName.toLowerCase().includes(searchLower)) {
          return false;
        }
      }
      if (txDateFrom && tx.transactionDate) {
        const txDate = new Date(tx.transactionDate);
        const fromDate = new Date(txDateFrom);
        if (txDate < fromDate) return false;
      }
      if (txDateTo && tx.transactionDate) {
        const txDate = new Date(tx.transactionDate);
        const toDate = new Date(txDateTo);
        toDate.setHours(23, 59, 59);
        if (txDate > toDate) return false;
      }
      return true;
    });
  }, [transactions, txSearchTerm, txDateFrom, txDateTo, txTypeFilter]);

  const clearTxFilters = () => {
    setTxSearchTerm('');
    setTxDateFrom('');
    setTxDateTo('');
    setTxTypeFilter('ALL');
  };

  const handleUnitChange = (unit: string) => {
    if (unit === 'CUSTOM') {
      setShowCustomUnitInput(true);
    } else {
      setShowCustomUnitInput(false);
      setItemForm({ ...itemForm, unit });
      setCustomUnit('');
    }
  };

  const handleCustomUnitBlur = () => {
    if (customUnit.trim()) {
      setItemForm({ ...itemForm, unit: customUnit.trim() });
      setShowCustomUnitInput(false);
    }
  };

  const handleCustomUnitKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && customUnit.trim()) {
      setItemForm({ ...itemForm, unit: customUnit.trim() });
      setShowCustomUnitInput(false);
    }
  };

  const handleSaveItem = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!itemForm.itemName) {
      showToast('error', 'Item name is required');
      return;
    }
    if (!itemForm.unit) {
      showToast('error', 'Unit is required');
      return;
    }

    try {
      if (editingItem && editingItem.id) {
        await api.stock.item.update(editingItem.id, itemForm);
        if (modalSelectedFile) {
          // Compress image before upload
          const compressedFile = await compressImage(modalSelectedFile, {
            maxWidth: 1200,
            maxHeight: 1200,
            quality: 0.85,
            maxSizeMB: 2
          });
          const imgFormData = new FormData();
          imgFormData.append('images', compressedFile);
          await api.stock.item.uploadImages(editingItem.id, imgFormData);
        }
        showToast('success', 'Stock item updated');
      } else {
        if (modalSelectedFile) {
          const compressedFile = await compressImage(modalSelectedFile, {
            maxWidth: 1200,
            maxHeight: 1200,
            quality: 0.85,
            maxSizeMB: 2
          });
          const formData = new FormData();
          formData.append('itemName', itemForm.itemName);
          formData.append('category', itemForm.category || 'GRAINS');
          formData.append('unit', itemForm.unit || 'kg');
          formData.append('currentStock', String(itemForm.currentStock || 0));
          formData.append('minimumStockLevel', String(itemForm.minimumStockLevel || 10));
          formData.append('unitPrice', String(itemForm.unitPrice || 0));
          formData.append('images', compressedFile);
          await api.stock.item.createWithImages(formData);
        } else {
          await api.stock.item.create(itemForm as StockItem);
        }
        showToast('success', 'New stock item added');
      }

      setIsItemModalOpen(false);
      setEditingItem(null);
      setModalSelectedFile(null);
      setShowCustomUnitInput(false);
      setCustomUnit('');
      resetItemForm();
      loadData();
    } catch (err: any) {
      showToast('error', err.message || 'Failed to save stock item');
    }
  };

  const handleAdjustStock = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!adjustModalItem || !adjustModalItem.id || adjustQty <= 0) return;

    try {
      if (adjustType === 'ADD') {
        await api.stock.item.addStock(adjustModalItem.id, adjustQty, 'PROCUREMENT', adjustNotes);
        showToast('success', `Added ${adjustQty} ${adjustModalItem.unit} to ${adjustModalItem.itemName}`);
      } else {
        await api.stock.item.deductStock(adjustModalItem.id, adjustQty, 'KITCHEN_USAGE', adjustNotes);
        showToast('info', `Deducted ${adjustQty} ${adjustModalItem.unit} from ${adjustModalItem.itemName}`);
      }
      setAdjustModalItem(null);
      setAdjustQty(10);
      setAdjustNotes('');
      loadData();
    } catch (err: any) {
      showToast('error', err.message || 'Failed to adjust stock level');
    }
  };

  const handleDeleteItem = async (item: StockItem) => {
    if (!item.id) return;
    if (window.confirm(`Are you sure you want to delete "${item.itemName}"?`)) {
      try {
        await api.stock.item.delete(item.id);
        showToast('info', `Deleted stock item ${item.itemName}`);
        loadData();
      } catch (err: any) {
        showToast('error', err.message || 'Failed to delete stock item');
      }
    }
  };

  const resetItemForm = () => {
    setItemForm({
      itemName: '',
      category: 'GRAINS',
      currentStock: 0,
      unit: 'kg',
      minimumStockLevel: 10,
      unitPrice: 50,
    });
    setModalSelectedFile(null);
    setShowCustomUnitInput(false);
    setCustomUnit('');
  };

  // Combined search term (global + local)
  const combinedSearchTerm = globalSearchTerm || localSearchTerm;

  const safeItems = Array.isArray(items) ? items : [];
  
  // Filter items
  const filteredItems = safeItems.filter(item => {
    if (!item) return false;
    if (categoryFilter !== 'ALL' && item.category !== categoryFilter) return false;
    if (combinedSearchTerm) {
      const q = combinedSearchTerm.toLowerCase();
      return (
        (item.itemName || '').toLowerCase().includes(q) ||
        (item.category || '').toLowerCase().includes(q)
      );
    }
    return true;
  });

  // Low stock items
  const lowStockItems = safeItems.filter(i => i && i.currentStock <= i.minimumStockLevel);
  const totalValue = safeItems.reduce((acc, i) => acc + ((i?.currentStock || 0) * (i?.unitPrice || 0)), 0);
  const lowStockCount = lowStockItems.length;

  return (
    <div className="space-y-6">
      {/* ========================================== */}
      {/* IMAGE PREVIEW MODAL */}
      {/* ========================================== */}
      {selectedImage && (
        <div 
          className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4 z-[100]"
          onClick={() => setSelectedImage(null)}
        >
          <div 
            className="bg-white rounded-xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
              <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
                <ImageIcon className="w-4 h-4 text-amber-500" />
                {selectedImage.name}
              </h3>
              <div className="flex items-center gap-2">
                <button
                  onClick={handleDownloadFromModal}
                  className="inline-flex items-center gap-2 px-3 py-1.5 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold rounded-lg text-xs transition-colors"
                >
                  <Download className="w-3.5 h-3.5" />
                  Download
                </button>
                <button
                  onClick={() => setSelectedImage(null)}
                  className="p-1.5 hover:bg-slate-100 rounded-lg text-slate-400 hover:text-slate-600 transition-colors"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>
            <div className="p-4 flex items-center justify-center bg-slate-50">
              <img
                src={selectedImage.url}
                alt={selectedImage.name}
                className="max-w-full max-h-[70vh] object-contain rounded-lg"
              />
            </div>
            <div className="px-6 py-3 border-t border-slate-200 bg-slate-50 flex items-center justify-between text-xs text-slate-500">
              <span>Click outside to close • Right-click to save</span>
              <span className="text-amber-600 font-medium">Click Download to save as WebP</span>
            </div>
          </div>
        </div>
      )}

      {/* Top Header & Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-xs text-slate-500 font-medium">Total Inventory Items</div>
            <div className="text-2xl font-bold text-slate-900 mt-0.5">{safeItems.length} Items</div>
          </div>
          <div className="p-2.5 bg-blue-50 text-blue-600 rounded-lg">
            <Boxes className="w-5 h-5" />
          </div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-xs text-slate-500 font-medium">Est. Total Pantry Value</div>
            <div className="text-2xl font-bold text-slate-900 mt-0.5">₹{totalValue.toLocaleString()}</div>
          </div>
          <div className="p-2.5 bg-emerald-50 text-emerald-600 rounded-lg">
            <IndianRupee className="w-5 h-5" />
          </div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-xs text-slate-500 font-medium">Low Stock Alerts</div>
            <div className="text-2xl font-bold text-slate-900 mt-0.5">
              <span className={lowStockCount > 0 ? 'text-rose-600 font-bold' : 'text-slate-900'}>
                {lowStockCount} Alert{lowStockCount === 1 ? '' : 's'}
              </span>
            </div>
          </div>
          <div className={`p-2.5 rounded-lg ${lowStockCount > 0 ? 'bg-rose-50 text-rose-600' : 'bg-slate-50 text-slate-400'}`}>
            <AlertTriangle className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Tabs & Actions Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
        <div className="flex items-center space-x-2 flex-wrap gap-2">
          <button
            onClick={() => setActiveTab('ITEMS')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${activeTab === 'ITEMS'
              ? 'bg-slate-900 text-white shadow-xs'
              : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
          >
            Pantry Inventory ({safeItems.length})
          </button>
          <button
            onClick={() => setActiveTab('LOW_STOCK')}
            className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${activeTab === 'LOW_STOCK'
              ? 'bg-rose-600 text-white shadow-xs'
              : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
          >
            <Bell className="w-3.5 h-3.5" />
            <span>Low Stock Alerts</span>
            {lowStockCount > 0 && (
              <span className="bg-rose-500 text-white text-[10px] px-1.5 py-0.5 rounded-full">
                {lowStockCount}
              </span>
            )}
          </button>
          <button
            onClick={() => setActiveTab('TRANSACTIONS')}
            className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${activeTab === 'TRANSACTIONS'
              ? 'bg-slate-900 text-white shadow-xs'
              : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
          >
            <History className="w-3.5 h-3.5" />
            <span>Audit Logs ({filteredTransactions.length})</span>
          </button>
        </div>

        <div className="flex items-center space-x-3">
          {activeTab === 'ITEMS' && (
            <div className="flex items-center bg-slate-100 p-1 rounded-lg border border-slate-200">
              <button
                onClick={() => setViewMode('CARD')}
                className={`p-1.5 rounded-md text-xs font-medium flex items-center space-x-1 transition-all ${viewMode === 'CARD'
                  ? 'bg-white text-slate-900 shadow-xs font-bold'
                  : 'text-slate-500 hover:text-slate-900'
                  }`}
                title="Card Grid View with Images"
              >
                <LayoutGrid className="w-3.5 h-3.5" />
                <span>Cards</span>
              </button>
              <button
                onClick={() => setViewMode('TABLE')}
                className={`p-1.5 rounded-md text-xs font-medium flex items-center space-x-1 transition-all ${viewMode === 'TABLE'
                  ? 'bg-white text-slate-900 shadow-xs font-bold'
                  : 'text-slate-500 hover:text-slate-900'
                  }`}
                title="Table List View"
              >
                <List className="w-3.5 h-3.5" />
                <span>List</span>
              </button>
            </div>
          )}

          <button
            onClick={() => {
              resetItemForm();
              setEditingItem(null);
              setIsItemModalOpen(true);
            }}
            className="inline-flex items-center space-x-2 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold px-4 py-2 rounded-lg text-xs shadow-xs transition-colors shrink-0"
          >
            <Plus className="w-4 h-4" />
            <span>Add New Stock Item</span>
          </button>
        </div>
      </div>

      {/* Search Bar for Items */}
      {(activeTab === 'ITEMS' || activeTab === 'LOW_STOCK') && (
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            type="text"
            placeholder="Search stock items by name or category..."
            value={localSearchTerm}
            onChange={(e) => setLocalSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 border border-slate-300 rounded-xl text-sm focus:ring-2 focus:ring-amber-500 focus:border-transparent bg-white"
          />
        </div>
      )}

      {activeTab === 'LOW_STOCK' ? (
        /* Low Stock Alerts Tab */
        <div className="space-y-4">
          {lowStockItems.length === 0 ? (
            <div className="bg-white rounded-xl border border-slate-200 p-12 text-center">
              <div className="w-16 h-16 bg-emerald-50 rounded-full flex items-center justify-center mx-auto mb-4">
                <CheckCircle className="w-8 h-8 text-emerald-500" />
              </div>
              <h3 className="text-lg font-semibold text-slate-900">All Stock Levels Healthy!</h3>
              <p className="text-sm text-slate-500 mt-1">No items are currently below their minimum stock threshold.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {lowStockItems.map(item => {
                const rawImg = item.primaryImageUrl || (item.imageUrls && item.imageUrls.length > 0 ? item.imageUrls[0] : null);
                const fallbackImg = categoryFallbackImages[item.category] || categoryFallbackImages.OTHERS;
                const itemImg = rawImg ? (rawImg.startsWith('http') ? rawImg : `http://localhost:8080${rawImg}`) : fallbackImg;

                return (
                  <div key={item.id} className="bg-white rounded-xl border border-rose-200 shadow-sm p-4 hover:shadow-md transition-shadow">
                    <div className="flex items-start gap-4">
                      <div 
                        className="cursor-pointer"
                        onClick={() => handleImageClick(itemImg, item.itemName, item.id)}
                      >
                        <img
                          src={itemImg}
                          alt={item.itemName}
                          className="w-16 h-16 object-cover rounded-lg border border-slate-200 hover:opacity-80 transition-opacity"
                          onError={(e) => {
                            const fallback = categoryFallbackImages[item.category] || categoryFallbackImages.OTHERS;
                            if (e.currentTarget.src !== fallback) {
                              e.currentTarget.src = fallback;
                            }
                          }}
                        />
                      </div>
                      <div className="flex-1 min-w-0">
                        <h4 className="font-semibold text-slate-900 truncate">{item.itemName}</h4>
                        <p className="text-xs text-slate-500">{item.category}</p>
                        <div className="mt-2 flex items-center gap-3">
                          <div className="bg-rose-50 px-2 py-1 rounded-lg">
                            <span className="text-xs font-bold text-rose-600">
                              {item.currentStock} {item.unit}
                            </span>
                            <span className="text-[10px] text-rose-400 ml-1">available</span>
                          </div>
                          <div className="text-xs text-slate-500">
                            Min: {item.minimumStockLevel} {item.unit}
                          </div>
                        </div>
                      </div>
                    </div>
                    <div className="mt-3 pt-3 border-t border-slate-100 flex gap-2">
                      <button
                        onClick={() => {
                          setAdjustModalItem(item);
                          setAdjustType('ADD');
                        }}
                        className="flex-1 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-lg transition-colors text-center"
                      >
                        + Restock Now
                      </button>
                      <button
                        onClick={() => {
                          setEditingItem(item);
                          setItemForm({ ...item });
                          setIsItemModalOpen(true);
                        }}
                        className="px-3 py-1.5 border border-slate-300 text-slate-600 hover:bg-slate-50 rounded-lg text-xs transition-colors"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      ) : activeTab === 'TRANSACTIONS' ? (
        /* Transactions Audit Tab with Filters */
        <div className="space-y-4">
          {/* Transaction Filters */}
          <div className="bg-white border border-slate-200 rounded-xl p-4 shadow-xs">
            <div className="flex items-center justify-between">
              <button
                onClick={() => setShowFilters(!showFilters)}
                className="flex items-center gap-2 text-sm font-semibold text-slate-700 hover:text-slate-900"
              >
                <Filter className="w-4 h-4" />
                Filters
                <ChevronDown className={`w-4 h-4 transition-transform ${showFilters ? 'rotate-180' : ''}`} />
              </button>
              {(txSearchTerm || txDateFrom || txDateTo || txTypeFilter !== 'ALL') && (
                <button
                  onClick={clearTxFilters}
                  className="text-xs text-blue-600 hover:text-blue-800 font-medium"
                >
                  Clear All Filters
                </button>
              )}
            </div>

            {showFilters && (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 mt-4">
                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">Search Item</label>
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-3.5 h-3.5 text-slate-400" />
                    <input
                      type="text"
                      placeholder="Search by item name..."
                      value={txSearchTerm}
                      onChange={(e) => setTxSearchTerm(e.target.value)}
                      className="w-full pl-9 pr-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-amber-500 focus:border-transparent"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">From Date</label>
                  <div className="relative">
                    <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 w-3.5 h-3.5 text-slate-400" />
                    <input
                      type="date"
                      value={txDateFrom}
                      onChange={(e) => setTxDateFrom(e.target.value)}
                      className="w-full pl-9 pr-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-amber-500 focus:border-transparent"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">To Date</label>
                  <div className="relative">
                    <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 w-3.5 h-3.5 text-slate-400" />
                    <input
                      type="date"
                      value={txDateTo}
                      onChange={(e) => setTxDateTo(e.target.value)}
                      className="w-full pl-9 pr-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-amber-500 focus:border-transparent"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-medium text-slate-600 mb-1">Transaction Type</label>
                  <select
                    value={txTypeFilter}
                    onChange={(e) => setTxTypeFilter(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-amber-500 focus:border-transparent bg-white"
                  >
                    <option value="ALL">All Types</option>
                    <option value="PURCHASE">Purchase</option>
                    <option value="USAGE">Usage</option>
                    <option value="RETURN">Return</option>
                    <option value="ADJUSTMENT">Adjustment</option>
                  </select>
                </div>
              </div>
            )}
          </div>

          {/* Transactions Table */}
          <div className="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-xs">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-semibold uppercase tracking-wider">
                    <th className="py-3 px-4">Date</th>
                    <th className="py-3 px-4">Item Name</th>
                    <th className="py-3 px-4">Action</th>
                    <th className="py-3 px-4">Quantity</th>
                    <th className="py-3 px-4">Cost (₹)</th>
                    <th className="py-3 px-4">Notes</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-slate-700">
                  {filteredTransactions.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="py-8 text-center text-slate-400">
                        {transactions.length === 0 ? (
                          <div className="space-y-2">
                            <History className="w-10 h-10 mx-auto text-slate-300" />
                            <p className="text-sm font-semibold">No transactions recorded yet.</p>
                          </div>
                        ) : (
                          <p className="text-sm font-semibold">No transactions match your filters.</p>
                        )}
                      </td>
                    </tr>
                  ) : (
                    filteredTransactions.map((tx, index) => {
                      let typeColor = 'bg-gray-50 text-gray-700';
                      let typeIcon = null;

                      if (tx.transactionType === 'PURCHASE') {
                        typeColor = 'bg-emerald-50 text-emerald-700';
                        typeIcon = <TrendingUp className="w-3 h-3 mr-1" />;
                      } else if (tx.transactionType === 'USAGE' || tx.transactionType === 'CONSUME') {
                        typeColor = 'bg-amber-50 text-amber-700';
                        typeIcon = <TrendingDown className="w-3 h-3 mr-1" />;
                      } else if (tx.transactionType === 'RETURN') {
                        typeColor = 'bg-blue-50 text-blue-700';
                        typeIcon = <TrendingUp className="w-3 h-3 mr-1" />;
                      } else {
                        typeColor = 'bg-purple-50 text-purple-700';
                      }

                      return (
                        <tr key={tx.id || index} className="hover:bg-slate-50/70 transition-colors">
                          <td className="py-3 px-4 font-mono text-[11px] text-slate-500">
                            {tx.transactionDate || '-'}
                          </td>
                          <td className="py-3 px-4 font-semibold text-slate-900 text-sm">
                            {tx.stockItemName || tx.itemName || 'Unknown Item'}
                          </td>
                          <td className="py-3 px-4">
                            <span className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold ${typeColor}`}>
                              {typeIcon} {tx.transactionType || 'UNKNOWN'}
                            </span>
                          </td>
                          <td className="py-3 px-4 font-bold text-slate-900">
                            {tx.quantity || 0} {tx.unit || ''}
                          </td>
                          <td className="py-3 px-4 text-slate-800 font-medium">
                            ₹{(tx.totalAmount || 0).toLocaleString()}
                          </td>
                          <td className="py-3 px-4 text-slate-500 text-[11px] truncate max-w-[150px]">
                            {tx.remarks || tx.referenceNumber || '-'}
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>

            {filteredTransactions.length > 0 && (
              <div className="px-4 py-3 bg-slate-50 border-t border-slate-200 flex items-center justify-between text-xs">
                <span className="text-slate-500">
                  Showing {filteredTransactions.length} of {transactions.length} transactions
                </span>
                <span className="font-semibold text-slate-700">
                  Total Value: ₹{filteredTransactions.reduce((sum, tx) => sum + (tx.totalAmount || 0), 0).toLocaleString()}
                </span>
              </div>
            )}
          </div>
        </div>
      ) : (
        /* ITEMS TAB - Continue with existing items rendering */
        <>
          {/* Category Filter Pills */}
          <div className="flex items-center space-x-2 overflow-x-auto pb-1 max-w-full">
            <button
              onClick={() => setCategoryFilter('ALL')}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold shrink-0 transition-all ${categoryFilter === 'ALL'
                ? 'bg-amber-500 text-slate-950 font-bold'
                : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-100'
                }`}
            >
              All Categories
            </button>
            {stockCategories.map(cat => (
              <button
                key={cat}
                onClick={() => setCategoryFilter(cat)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold shrink-0 transition-all ${categoryFilter === cat
                  ? 'bg-amber-500 text-slate-950 font-bold'
                  : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-100'
                  }`}
              >
                {cat}
              </button>
            ))}
          </div>

          {/* Items Display */}
          {viewMode === 'CARD' ? (
            /* Card Grid View */
            filteredItems.length === 0 ? (
              <div className="bg-white rounded-xl border border-slate-200 p-12 text-center text-slate-400 my-4">
                <Boxes className="w-12 h-12 mx-auto text-slate-300 mb-2" />
                <p className="text-sm font-semibold">No stock items match your filter criteria.</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
                {filteredItems.map(item => {
                  const isLow = item.currentStock <= item.minimumStockLevel;
                  const rawImg = item.primaryImageUrl || (item.imageUrls && item.imageUrls.length > 0 ? item.imageUrls[0] : null);
                  const fallbackImg = categoryFallbackImages[item.category] || categoryFallbackImages.OTHERS;
                  const itemImg = rawImg ? (rawImg.startsWith('http') ? rawImg : `http://localhost:8080${rawImg}`) : fallbackImg;

                  return (
                    <div
                      key={item.id || Math.random()}
                      className={`bg-white rounded-xl border overflow-hidden shadow-xs hover:shadow-md transition-all flex flex-col justify-between ${isLow ? 'border-rose-300 ring-1 ring-rose-300/50' : 'border-slate-200'
                        }`}
                    >
                      <div className="relative h-44 bg-slate-100 overflow-hidden group">
                        {/* Clickable Image - opens preview modal */}
                        <div 
                          className="w-full h-full cursor-pointer"
                          onClick={() => handleImageClick(itemImg, item.itemName, item.id)}
                        >
                          <img
                            src={itemImg}
                            alt={item.itemName}
                            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                            onError={(e) => {
                              const fallback = categoryFallbackImages[item.category] || categoryFallbackImages.OTHERS;
                              if (e.currentTarget.src !== fallback) {
                                e.currentTarget.src = fallback;
                              }
                            }}
                          />
                          {/* Hover overlay with download hint */}
                          <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                            <div className="bg-white/90 rounded-lg px-3 py-1.5 flex items-center gap-2 text-slate-800 font-medium text-xs shadow-lg">
                              <Eye className="w-3.5 h-3.5" />
                              <span>Click to view</span>
                              <Download className="w-3.5 h-3.5 ml-1" />
                            </div>
                          </div>
                        </div>

                        <div className="absolute top-3 left-3 bg-slate-900/80 backdrop-blur-md text-white text-[10px] font-bold uppercase tracking-wider px-2.5 py-1 rounded-md shadow-xs">
                          {item.category}
                        </div>

                        <div className="absolute top-3 right-3">
                          {isLow ? (
                            <span className="inline-flex items-center px-2.5 py-1 rounded-md text-[10px] font-bold bg-rose-600 text-white shadow-xs animate-pulse">
                              <AlertTriangle className="w-3 h-3 mr-1" /> LOW STOCK
                            </span>
                          ) : (
                            <span className="inline-flex items-center px-2.5 py-1 rounded-md text-[10px] font-bold bg-emerald-600 text-white shadow-xs">
                              HEALTHY
                            </span>
                          )}
                        </div>

                        <label
                          title="Upload / Change Image"
                          className="absolute bottom-3 right-3 bg-white/90 hover:bg-white text-slate-800 p-2 rounded-full shadow-md cursor-pointer transition-colors opacity-90 hover:opacity-100"
                        >
                          <Camera className="w-3.5 h-3.5" />
                          <input
                            type="file"
                            accept="image/*"
                            className="hidden"
                            onChange={e => {
                              if (e.target.files && e.target.files[0] && item.id) {
                                handleCardImageUpload(item.id, e.target.files[0]);
                              }
                            }}
                          />
                        </label>
                      </div>

                      <div className="p-4 space-y-3 flex-1 flex flex-col justify-between">
                        <div>
                          <div className="flex items-start justify-between gap-2">
                            <h3 className="font-bold text-slate-900 text-base line-clamp-1">{item.itemName}</h3>
                            <div className="text-xs font-bold text-slate-700 bg-slate-100 px-2 py-0.5 rounded shrink-0">
                              ₹{item.unitPrice || 0}/{item.unit || 'kg'}
                            </div>
                          </div>

                          <div className="mt-3 grid grid-cols-2 gap-2 bg-slate-50 p-2.5 rounded-lg border border-slate-100 text-xs">
                            <div>
                              <div className="text-[10px] text-slate-400 font-medium uppercase">Available</div>
                              <div className={`font-bold text-sm mt-0.5 ${isLow ? 'text-rose-600' : 'text-slate-900'}`}>
                                {item.currentStock} {item.unit}
                              </div>
                            </div>
                            <div>
                              <div className="text-[10px] text-slate-400 font-medium uppercase">Min Alert</div>
                              <div className="font-bold text-sm text-slate-600 mt-0.5">
                                {item.minimumStockLevel} {item.unit}
                              </div>
                            </div>
                          </div>

                          <div className="mt-2.5 flex items-center justify-between text-[11px] text-slate-500">
                            <span>Est. Total Value:</span>
                            <span className="font-bold text-slate-800">
                              ₹{((item.currentStock || 0) * (item.unitPrice || 0)).toLocaleString()}
                            </span>
                          </div>
                        </div>

                        <div className="pt-3 border-t border-slate-100 flex items-center space-x-1.5">
                          <button
                            onClick={() => {
                              setAdjustModalItem(item);
                              setAdjustType('ADD');
                            }}
                            className="flex-1 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-lg transition-colors shadow-2xs text-center"
                          >
                            + Restock
                          </button>
                          <button
                            onClick={() => {
                              setAdjustModalItem(item);
                              setAdjustType('DEDUCT');
                            }}
                            className="flex-1 py-1.5 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold text-xs rounded-lg transition-colors shadow-2xs text-center"
                          >
                            - Consume
                          </button>
                          <button
                            onClick={() => {
                              setEditingItem(item);
                              setItemForm({ ...item });
                              setIsItemModalOpen(true);
                            }}
                            className="p-1.5 hover:bg-slate-100 text-slate-600 rounded-lg transition-colors"
                            title="Edit Item Details"
                          >
                            <Edit2 className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDeleteItem(item)}
                            className="p-1.5 hover:bg-rose-50 text-rose-600 rounded-lg transition-colors"
                            title="Delete Item"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )
          ) : (
            /* Table List View */
            <div className="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-xs">
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse text-xs">
                  <thead>
                    <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-semibold uppercase tracking-wider">
                      <th className="py-3 px-4">Image</th>
                      <th className="py-3 px-4">Item Name</th>
                      <th className="py-3 px-4">Category</th>
                      <th className="py-3 px-4">Current Stock</th>
                      <th className="py-3 px-4">Min. Threshold</th>
                      <th className="py-3 px-4">Est. Value (₹)</th>
                      <th className="py-3 px-4">Status</th>
                      <th className="py-3 px-4 text-right">Stock Action</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 text-slate-700">
                    {filteredItems.length === 0 ? (
                      <tr>
                        <td colSpan={8} className="py-8 text-center text-slate-400">
                          No inventory items found.
                        </td>
                      </tr>
                    ) : (
                      filteredItems.map(item => {
                        const isLow = item.currentStock <= item.minimumStockLevel;
                        const rawImg = item.primaryImageUrl || (item.imageUrls && item.imageUrls.length > 0 ? item.imageUrls[0] : null);
                        const fallbackImg = categoryFallbackImages[item.category] || categoryFallbackImages.OTHERS;
                        const itemImg = rawImg ? (rawImg.startsWith('http') ? rawImg : `http://localhost:8080${rawImg}`) : fallbackImg;

                        return (
                          <tr key={item.id || Math.random()} className="hover:bg-slate-50/70 transition-colors">
                            <td className="py-2.5 px-4">
                              <div 
                                className="cursor-pointer"
                                onClick={() => handleImageClick(itemImg, item.itemName, item.id)}
                              >
                                <img
                                  src={itemImg}
                                  alt={item.itemName}
                                  className="w-10 h-10 object-cover rounded-lg border border-slate-200 hover:opacity-80 transition-opacity"
                                  onError={(e) => {
                                    const fallback = categoryFallbackImages[item.category] || categoryFallbackImages.OTHERS;
                                    if (e.currentTarget.src !== fallback) {
                                      e.currentTarget.src = fallback;
                                    }
                                  }}
                                />
                              </div>
                            </td>
                            <td className="py-3.5 px-4 font-semibold text-slate-900 text-sm">
                              {item.itemName}
                            </td>
                            <td className="py-3.5 px-4">
                              <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-100 text-slate-600">
                                {item.category}
                              </span>
                            </td>
                            <td className="py-3.5 px-4 font-bold text-slate-900 text-sm">
                              {item.currentStock} {item.unit}
                            </td>
                            <td className="py-3.5 px-4 text-slate-500">
                              {item.minimumStockLevel} {item.unit}
                            </td>
                            <td className="py-3.5 px-4 font-semibold text-slate-800">
                              ₹{((item.currentStock || 0) * (item.unitPrice || 0)).toLocaleString()}
                            </td>
                            <td className="py-3.5 px-4">
                              {isLow ? (
                                <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold bg-rose-50 text-rose-700 border border-rose-200">
                                  <AlertTriangle className="w-3 h-3 mr-1" /> Low Stock
                                </span>
                              ) : (
                                <span className="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-50 text-emerald-700">
                                  Healthy
                                </span>
                              )}
                            </td>
                            <td className="py-3.5 px-4 text-right space-x-1.5 whitespace-nowrap">
                              <button
                                onClick={() => {
                                  setAdjustModalItem(item);
                                  setAdjustType('ADD');
                                }}
                                className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded font-bold text-[11px] transition-colors shadow-2xs"
                              >
                                + Add
                              </button>
                              <button
                                onClick={() => {
                                  setAdjustModalItem(item);
                                  setAdjustType('DEDUCT');
                                }}
                                className="px-2.5 py-1 bg-amber-50 hover:bg-amber-100 text-amber-800 border border-amber-200 rounded font-bold text-[11px] transition-colors"
                              >
                                - Use
                              </button>
                              <button
                                onClick={() => {
                                  setEditingItem(item);
                                  setItemForm({ ...item });
                                  setIsItemModalOpen(true);
                                }}
                                className="p-1 hover:bg-slate-100 text-slate-500 rounded"
                              >
                                <Edit2 className="w-3.5 h-3.5" />
                              </button>
                              <button
                                onClick={() => handleDeleteItem(item)}
                                className="p-1 hover:bg-rose-50 text-rose-600 rounded"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </td>
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      )}

      {/* Adjust Stock Modal */}
      {adjustModalItem && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl shadow-2xl border border-slate-200 max-w-sm w-full overflow-hidden">
            <div className="px-5 py-4 border-b border-slate-200 flex items-center justify-between">
              <h3 className="font-bold text-slate-900 text-sm">
                {adjustType === 'ADD' ? 'Add Stock (Procurement)' : 'Deduct Stock (Kitchen Use)'}
              </h3>
              <button onClick={() => setAdjustModalItem(null)} className="text-slate-400 hover:text-slate-600">
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleAdjustStock} className="p-5 space-y-4 text-xs">
              <div>
                <div className="font-bold text-slate-900 text-sm">{adjustModalItem.itemName}</div>
                <div className="text-[11px] text-slate-500 mt-0.5">
                  Current Stock: <strong>{adjustModalItem.currentStock} {adjustModalItem.unit}</strong>
                </div>
              </div>

              <div>
                <label className="block text-slate-700 font-semibold mb-1">
                  Quantity ({adjustModalItem.unit}) *
                </label>
                <input
                  type="number"
                  min="0.1"
                  step="any"
                  required
                  value={adjustQty}
                  onChange={e => setAdjustQty(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-slate-700 font-semibold mb-1">Reason / Notes</label>
                <input
                  type="text"
                  placeholder={adjustType === 'ADD' ? 'Weekly purchase from wholesaler' : 'Lunch preparation consumption'}
                  value={adjustNotes}
                  onChange={e => setAdjustNotes(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none"
                />
              </div>

              <div className="pt-3 border-t border-slate-100 flex items-center justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setAdjustModalItem(null)}
                  className="px-3 py-1.5 border border-slate-300 rounded-lg text-slate-700 font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className={`px-4 py-1.5 font-bold rounded-lg text-white shadow-xs transition-colors ${adjustType === 'ADD' ? 'bg-emerald-600 hover:bg-emerald-700' : 'bg-amber-600 hover:bg-amber-700'
                    }`}
                >
                  Confirm {adjustType === 'ADD' ? 'Addition' : 'Deduction'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add / Edit Item Modal */}
      {isItemModalOpen && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl shadow-2xl border border-slate-200 max-w-md w-full overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
              <h3 className="font-bold text-slate-900 text-sm">
                {editingItem ? 'Edit Pantry Item' : 'Add New Pantry Item'}
              </h3>
              <button
                onClick={() => {
                  setIsItemModalOpen(false);
                  setShowCustomUnitInput(false);
                  setCustomUnit('');
                }}
                className="text-slate-400 hover:text-slate-600"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleSaveItem} className="p-6 space-y-4 text-xs">
              <div>
                <label className="block text-slate-700 font-semibold mb-1">Item Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Arhar Dal, Atta, Mustard Oil"
                  value={itemForm.itemName || ''}
                  onChange={e => setItemForm({ ...itemForm, itemName: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-slate-700 font-semibold mb-1">Item Image (Optional)</label>
                <div className="flex items-center space-x-2">
                  <input
                    type="file"
                    accept="image/*"
                    onChange={e => {
                      if (e.target.files && e.target.files[0]) {
                        setModalSelectedFile(e.target.files[0]);
                      }
                    }}
                    className="w-full text-xs text-slate-500 file:mr-3 file:py-1.5 file:px-3 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-slate-100 file:text-slate-700 hover:file:bg-slate-200 cursor-pointer"
                  />
                </div>
                {modalSelectedFile && (
                  <p className="text-[11px] text-emerald-600 font-medium mt-1">
                    Selected file: {modalSelectedFile.name} (will be compressed to WebP)
                  </p>
                )}
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-700 font-semibold mb-1">Category</label>
                  <select
                    value={itemForm.category || 'GRAINS'}
                    onChange={e => setItemForm({ ...itemForm, category: e.target.value as any })}
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none bg-white"
                  >
                    {stockCategories.map(cat => (
                      <option key={cat} value={cat}>
                        {cat}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-slate-700 font-semibold mb-1">Unit *</label>
                  {!showCustomUnitInput ? (
                    <div className="flex gap-2">
                      <select
                        value={itemForm.unit || 'kg'}
                        onChange={(e) => handleUnitChange(e.target.value)}
                        className="flex-1 px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none bg-white"
                        required
                      >
                        {PREDEFINED_UNITS.map(unit => (
                          <option key={unit} value={unit}>
                            {unit}
                          </option>
                        ))}
                        <option value="CUSTOM">➕ Add Custom Unit...</option>
                      </select>
                    </div>
                  ) : (
                    <div className="flex gap-2">
                      <input
                        type="text"
                        placeholder="Enter unit name..."
                        value={customUnit}
                        onChange={(e) => setCustomUnit(e.target.value)}
                        onBlur={handleCustomUnitBlur}
                        onKeyPress={handleCustomUnitKeyPress}
                        className="flex-1 px-3 py-2 border border-amber-400 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none bg-amber-50"
                        autoFocus
                      />
                      <button
                        type="button"
                        onClick={() => {
                          if (customUnit.trim()) {
                            setItemForm({ ...itemForm, unit: customUnit.trim() });
                            setShowCustomUnitInput(false);
                          }
                        }}
                        className="px-3 py-2 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold rounded-lg transition-colors"
                      >
                        Add
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setShowCustomUnitInput(false);
                          setCustomUnit('');
                        }}
                        className="px-3 py-2 border border-slate-300 rounded-lg text-slate-600 hover:bg-slate-50"
                      >
                        Cancel
                      </button>
                    </div>
                  )}
                  {itemForm.unit && !showCustomUnitInput && (
                    <div className="mt-1.5 flex items-center gap-2">
                      <span className="text-[11px] text-slate-500">Selected unit:</span>
                      <span className="text-[11px] font-bold text-slate-700 bg-slate-100 px-2 py-0.5 rounded">
                        {itemForm.unit}
                      </span>
                      <button
                        type="button"
                        onClick={() => {
                          setShowCustomUnitInput(true);
                          setCustomUnit('');
                        }}
                        className="text-[11px] text-blue-600 hover:text-blue-800"
                      >
                        Change
                      </button>
                    </div>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-slate-700 font-semibold mb-1">Initial Stock</label>
                  <input
                    type="number"
                    step="any"
                    min="0"
                    value={itemForm.currentStock === 0 ? '' : itemForm.currentStock}
                    onChange={e => {
                      const val = e.target.value === '' ? 0 : Number(e.target.value);
                      setItemForm({ ...itemForm, currentStock: val });
                    }}
                    onFocus={(e) => {
                      if (e.target.value === '0') {
                        e.target.value = '';
                      }
                    }}
                    onBlur={(e) => {
                      if (e.target.value === '') {
                        setItemForm({ ...itemForm, currentStock: 0 });
                      }
                    }}
                    placeholder="0"
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-slate-700 font-semibold mb-1">Min. Alert Qty</label>
                  <input
                    type="number"
                    step="any"
                    min="0"
                    value={itemForm.minimumStockLevel === 0 ? '' : itemForm.minimumStockLevel}
                    onChange={e => {
                      const val = e.target.value === '' ? 0 : Number(e.target.value);
                      setItemForm({ ...itemForm, minimumStockLevel: val });
                    }}
                    onFocus={(e) => {
                      if (e.target.value === '0') {
                        e.target.value = '';
                      }
                    }}
                    onBlur={(e) => {
                      if (e.target.value === '') {
                        setItemForm({ ...itemForm, minimumStockLevel: 0 });
                      }
                    }}
                    placeholder="0"
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-slate-700 font-semibold mb-1">Unit Price (₹)</label>
                  <input
                    type="number"
                    step="any"
                    min="0"
                    value={itemForm.unitPrice === 0 ? '' : itemForm.unitPrice}
                    onChange={e => {
                      const val = e.target.value === '' ? 0 : Number(e.target.value);
                      setItemForm({ ...itemForm, unitPrice: val });
                    }}
                    onFocus={(e) => {
                      if (e.target.value === '0') {
                        e.target.value = '';
                      }
                    }}
                    onBlur={(e) => {
                      if (e.target.value === '') {
                        setItemForm({ ...itemForm, unitPrice: 0 });
                      }
                    }}
                    placeholder="0"
                    className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none"
                  />
                </div>
              </div>

              <div className="pt-4 border-t border-slate-100 flex items-center justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => {
                    setIsItemModalOpen(false);
                    setShowCustomUnitInput(false);
                    setCustomUnit('');
                  }}
                  className="px-4 py-2 border border-slate-300 rounded-lg text-slate-700 font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold rounded-lg shadow-xs"
                >
                  Save Item
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};