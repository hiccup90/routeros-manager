package com.routeros.manager.ui.network.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.routeros.manager.ui.components.GlassCard

@Composable
fun DetailListSkeleton(itemCount: Int = 4) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items((1..itemCount).toList()) {
            DetailSkeletonCard()
        }
    }
}

@Composable
private fun DetailSkeletonCard() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonLine(width = 128.dp, height = 18.dp, alpha = 0.28f)
                    SkeletonLine(width = 172.dp, height = 14.dp, alpha = 0.18f)
                }
                SkeletonChip(width = 52.dp, height = 24.dp, alpha = 0.18f)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonChip(width = 74.dp, height = 24.dp, alpha = 0.16f)
                SkeletonChip(width = 96.dp, height = 24.dp, alpha = 0.12f)
            }
        }
    }
}

@Composable
fun SummarySkeletonCard() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonLine(width = 110.dp, height = 16.dp, alpha = 0.22f)
            SkeletonLine(width = 80.dp, height = 32.dp, alpha = 0.30f)
            SkeletonLine(width = 160.dp, height = 14.dp, alpha = 0.16f)
        }
    }
}

@Composable
private fun SkeletonLine(width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp, alpha: Float) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
    ) {
        Box(modifier = Modifier.width(width).height(height))
    }
}

@Composable
private fun SkeletonChip(width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp, alpha: Float) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
    ) {
        Box(modifier = Modifier.width(width).height(height))
    }
}
